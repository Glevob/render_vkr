package ru.accouting.student.controller;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.accouting.student.dto.ContestProtocolRowDto;
import ru.accouting.student.model.MilitaryAccountingSpecialtyEntity;
import ru.accouting.student.service.ContestProtocolService;
import ru.accouting.student.service.MilitaryAccountingSpecialtyService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/contest-protocol")
@RequiredArgsConstructor
public class ContestProtocolController {

    private final ContestProtocolService contestProtocolService;
    private final MilitaryAccountingSpecialtyService specialtyService;

    @GetMapping
    public String showContestProtocol(
            @RequestParam(name = "limit", defaultValue = "144") int limit,
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "specialtyCode", required = false) String specialtyCode,
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication authentication,
            Model model
    ) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        boolean isUser = authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER"));

        List<MilitaryAccountingSpecialtyEntity> specialties = specialtyService.findAll();
        model.addAttribute("specialties", specialties);

        model.addAttribute("selectedYear", targetYear);
        model.addAttribute("availableYears", contestProtocolService.getAvailableYears());
        model.addAttribute("limit", limit);
        model.addAttribute("selectedSpecialtyCode", specialtyCode);

        // Если зашел Студент, то покажет протокол только для него
        if (isUser) {
            List<ContestProtocolRowDto> userRows = contestProtocolService.getRowsByYearForCurrentUser(targetYear);

            model.addAttribute("effectiveLimit", userRows.size());
            model.addAttribute("totalEligibleCount", userRows.size());
            model.addAttribute("eligibleCount", userRows.size());
            model.addAttribute("notEligibleCount", 0);
            model.addAttribute("passedWithinLimit", userRows);
            model.addAttribute("passedOverLimit", List.of());
            model.addAttribute("failedPrelim", List.of());

            model.addAttribute("countSelected", userRows.size());
            model.addAttribute("countAll", 0);

            return "contest/protocol";
        }

        // Загружаем полный список строк протокола для всех студентов за указанный год
        List<ContestProtocolRowDto> allStudents = contestProtocolService.getAllRowsByYear(targetYear);

        // Отбираем студентов, допущенных к конкурсу, и выстраиваем их в рейтинговый список
        List<ContestProtocolRowDto> allEligible = allStudents.stream()
                // Оставляем только тех, кто успешно прошел предварительный отбор
                .filter(contestProtocolService::isEligible)
                // Сортируем по убыванию финального балла
                .sorted(Comparator
                        .comparingInt(contestProtocolService::getPriorityIndex)
                        .thenComparing(ContestProtocolRowDto::getFinalScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        // Вычисляем количество успешно прошедших отбор и финальный лимит зачисления
        int totalEligibleCount = allEligible.size();               // Общее число успешных кандидатов
        int effectiveLimit = Math.min(limit, totalEligibleCount);  // Реальный лимит (не может превышать число кандидатов)

        // Формируем список студентов, которые проходят в рамках выделенного лимита (расчет потребности)
        List<ContestProtocolRowDto> allPassedWithinLimit = allEligible.stream()
                .limit(effectiveLimit) // Берем первые N человек, попавших в лимит
                // Каждому проставляем положительное решение комиссии
                .map(row -> { row.setCommissionDecision("Рекомендовать для допуска"); return row; })
                .toList();

        // Формируем список студентов, которые прошли отбор, но не поместились в выделенный лимит
        List<ContestProtocolRowDto> allPassedOverLimit = allEligible.stream()
                .skip(effectiveLimit) // Пропускаем тех, кто уже попал в лимит, и берем оставшихся
                // Каждому проставляем причину отказа по конкурсу баллов
                .map(row -> { row.setCommissionDecision("Отказать по конкурсу"); return row; })
                .toList();

        // Формируем список студентов, которые провалили отбор еще на предварительном этапе
        List<ContestProtocolRowDto> allFailedPrelim = allStudents.stream()
                .filter(row -> !contestProtocolService.isEligible(row)) // Оставляем только недопущенных
                // Генерируем индивидуальную причину отказа (Например, по категори годности, не прошел ФП)
                .map(row -> { row.setCommissionDecision(contestProtocolService.generateFailedReasons(row)); return row; })
                .toList();

        List<ContestProtocolRowDto> passedWithinLimit, passedOverLimit, failedPrelim;

        // Фильтр по программе подгготовки
        if (specialtyCode != null && !specialtyCode.trim().isEmpty() && !"ALL".equalsIgnoreCase(specialtyCode)) {
            passedWithinLimit = allPassedWithinLimit.stream()
                    .filter(row -> specialtyCode.equals(row.getProgramCode()))
                    .toList();
            passedOverLimit = allPassedOverLimit.stream()
                    .filter(row -> specialtyCode.equals(row.getProgramCode()))
                    .toList();
            failedPrelim = allFailedPrelim.stream()
                    .filter(row -> specialtyCode.equals(row.getProgramCode()))
                    .toList();
        } else {
            passedWithinLimit = allPassedWithinLimit;
            passedOverLimit = allPassedOverLimit;
            failedPrelim = allFailedPrelim;
        }

        long countSelected = passedWithinLimit.stream()
                .filter(row -> specialtyCode != null && specialtyCode.equals(row.getProgramCode()))
                .count();

        long countAll = passedWithinLimit.size();

        model.addAttribute("countSelected", countSelected);
        model.addAttribute("countAll", countAll);

        int eligibleCount = passedWithinLimit.size();
        int notEligibleCount = passedOverLimit.size() + failedPrelim.size();

        model.addAttribute("effectiveLimit", effectiveLimit);
        model.addAttribute("totalEligibleCount", totalEligibleCount);
        model.addAttribute("eligibleCount", eligibleCount);
        model.addAttribute("notEligibleCount", notEligibleCount);
        model.addAttribute("passedWithinLimit", passedWithinLimit);
        model.addAttribute("passedOverLimit", passedOverLimit);
        model.addAttribute("failedPrelim", failedPrelim);

        return "contest/protocol";
    }
    @PostMapping("/update-statuses")
    public String updateStatuses(
            @RequestParam(name = "limit", defaultValue = "144") int limit,
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "specialtyCode", required = false) String specialtyCode,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        // Вычисляем целевой год один раз
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        // Формируем базовую строку редиректа с сохранением года и лимита
        String redirectUrl = "redirect:/contest-protocol?year=" + targetYear + "&limit=" + limit;

        // Если была выбрана программа подготовки, сохраняем её в URL редиректа
        if (specialtyCode != null && !specialtyCode.isEmpty()) {
            redirectUrl += "&specialtyCode=" + specialtyCode;
        }

        // Проверка прав доступа
        boolean isUser = authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER"));

        if (isUser) {
            redirectAttributes.addFlashAttribute("errorMessage", "У вас нет доступа к обновлению статусов");
            return redirectUrl;
        }

        // Проверяем, есть ли уже распределенные по взводам студенты
        boolean hasDistributed = contestProtocolService.isProtocolLocked(targetYear, specialtyCode);
        if (hasDistributed) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Запрещено пересчитывать протокол! Часть студентов за этот год уже распределена по взводам. Сначала сбросьте распределение.");
            return redirectUrl;
        }

        contestProtocolService.updateContestStatuses(targetYear, limit);

        redirectAttributes.addFlashAttribute("successMessage", true);

        return redirectUrl;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportProtocols(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "specialtyCode", required = false) String specialtyCode,
            @RequestParam(name = "limit", defaultValue = "144") int limit) throws IOException {

        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        // Получаем полные данные
        List<ContestProtocolRowDto> allStudents = contestProtocolService.getAllRowsByYear(targetYear);

        // Логика расчета статусов
        List<ContestProtocolRowDto> allEligible = allStudents.stream()
                .filter(contestProtocolService::isEligible)
                .sorted(Comparator
                        .comparingInt(contestProtocolService::getPriorityIndex)
                        .thenComparing(ContestProtocolRowDto::getFinalScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int totalEligibleCount = allEligible.size();
        int effectiveLimit = Math.min(limit, totalEligibleCount);

        // Устанавливаем статусы
        List<ContestProtocolRowDto> allPassedWithinLimit = allEligible.stream()
                .limit(effectiveLimit)
                .map(row -> { row.setCommissionDecision("Рекомендовать для допуска"); return row; })
                .toList();

        List<ContestProtocolRowDto> allPassedOverLimit = allEligible.stream()
                .skip(effectiveLimit)
                .map(row -> { row.setCommissionDecision("Отказать по конкурсу"); return row; })
                .toList();

        List<ContestProtocolRowDto> allFailedPrelim = allStudents.stream()
                .filter(row -> !contestProtocolService.isEligible(row))
                .map(row -> { row.setCommissionDecision(contestProtocolService.generateFailedReasons(row)); return row; })
                .toList();

        // Логика фильтрации по программе подготовки
        List<ContestProtocolRowDto> passedWithinLimit, passedOverLimit, failedPrelim;

        if (specialtyCode != null && !specialtyCode.trim().isEmpty() && !"ALL".equalsIgnoreCase(specialtyCode)) {
            passedWithinLimit = allPassedWithinLimit.stream().filter(row -> specialtyCode.equals(row.getProgramCode())).toList();
            passedOverLimit = allPassedOverLimit.stream().filter(row -> specialtyCode.equals(row.getProgramCode())).toList();
            failedPrelim = allFailedPrelim.stream().filter(row -> specialtyCode.equals(row.getProgramCode())).toList();
        } else {
            passedWithinLimit = allPassedWithinLimit;
            passedOverLimit = allPassedOverLimit;
            failedPrelim = allFailedPrelim;
        }

        // Сбор списка в том же порядке (внутри Excel будет этот порядок)
        List<ContestProtocolRowDto> finalExportList = new ArrayList<>();
        finalExportList.addAll(passedWithinLimit);
        finalExportList.addAll(passedOverLimit);
        finalExportList.addAll(failedPrelim);

        // Выгрузка в Excel
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Если выбрана конкретная программа, выгружаем один файл
            addExcelToZip(zos, "Protocol_" + targetYear + ".xlsx", "Протокол", finalExportList);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Protocols_" + targetYear + ".zip");
        return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    }

    private void addExcelToZip(ZipOutputStream zos, String filename, String sheetName, List<ContestProtocolRowDto> students) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Список заголовков
            String[] headers = {
                    "№ п/п", "ФИО, дата рождения", "Код специальности В ВУЗе", "категория годности", "категория ППО",
                    "возрастная группа", "номер упражнения 1", "результат 1", "балл 1", "номер упражнения 2",
                    "результат 2", "балл 2", "номер упражнения 3", "результат 3", "балл 3",
                    "Общий балл ФП", "соответствие по уровню физ подготовки", "ФП (100-б.)",
                    "Право допуска", "Преимущественное право допуска", "Учёба (100-б.)", "Итоговый балл", "Решение конкурсной комиссии", "Программа подготовки"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) headerRow.createCell(i).setCellValue(headers[i]);

            int rowIdx = 1, counter = 1;
            for (ContestProtocolRowDto s : students) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;

                row.createCell(col++).setCellValue(counter++);
                row.createCell(col++).setCellValue((s.getFullName() != null ? s.getFullName() : "") + ", " + (s.getBirthDate() != null ? s.getBirthDate() : ""));
                row.createCell(col++).setCellValue(s.getUniversitySpecialtyCode() != null ? s.getUniversitySpecialtyCode() : "—");

                row.createCell(col++).setCellValue(s.getFitnessCategoryLabel());

                row.createCell(col++).setCellValue(s.getPsychoCategory() != null ? s.getPsychoCategory() : "");
                row.createCell(col++).setCellValue(s.getAgeGroup() != null ? s.getAgeGroup() : "");
                row.createCell(col++).setCellValue(s.getStrengthExerciseNumber() != null ? s.getStrengthExerciseNumber() : 0);
                row.createCell(col++).setCellValue(s.getStrengthResult() != null ? s.getStrengthResult() : "");
                row.createCell(col++).setCellValue(s.getStrengthPoints() != null ? s.getStrengthPoints() : 0);
                row.createCell(col++).setCellValue(s.getSpeedExerciseNumber() != null ? s.getSpeedExerciseNumber() : 0);
                row.createCell(col++).setCellValue(s.getSpeedResult() != null ? s.getSpeedResult() : "");
                row.createCell(col++).setCellValue(s.getSpeedPoints() != null ? s.getSpeedPoints() : 0);
                row.createCell(col++).setCellValue(s.getEnduranceExerciseNumber() != null ? s.getEnduranceExerciseNumber() : 0);
                row.createCell(col++).setCellValue(s.getEnduranceResult() != null ? s.getEnduranceResult() : "");
                row.createCell(col++).setCellValue(s.getEndurancePoints() != null ? s.getEndurancePoints() : 0);
                row.createCell(col++).setCellValue(s.getTotalPoints());
                row.createCell(col++).setCellValue(s.getPhysicalRequirementsMatch() != null ? s.getPhysicalRequirementsMatch() : "");
                row.createCell(col++).setCellValue(s.getPhysical100() != null ? s.getPhysical100() : 0);
                row.createCell(col++).setCellValue("нет");
                row.createCell(col++).setCellValue(s.getNoteStudent() != null ? s.getNoteStudent() : "");
                row.createCell(col++).setCellValue(s.getAcademic100() != null ? s.getAcademic100() : 0);
                row.createCell(col++).setCellValue(s.getFinalScore());

                row.createCell(col++).setCellValue(s.getCommissionDecisionFormatted());

                row.createCell(col++).setCellValue(s.getProgramCode() != null ? s.getProgramCode() : "—");
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            ZipEntry entry = new ZipEntry(filename);
            zos.putNextEntry(entry);
            workbook.write(zos);
            zos.closeEntry();
        }
    }
}