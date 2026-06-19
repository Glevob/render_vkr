package ru.accouting.student.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

 /*
 Класс для симметричного шифрования и дешифрования данных
 Используется алгоритм AES-GCM:
 симметричный блочный шифр, который обеспечивает как конфиденциальность данных,
 так и их аутентификацию (проверку целостности).
 Режим GCM сочетает шифрование в режиме счётчика с функцией аутентификации на основе поля Галуа
 */
public class AesGcmEncryptor {

    private static final String ALGO = "AES/GCM/NoPadding";       // Задает стандарт шифрования AES в режиме GCM
    private static final int IV_SIZE = 12;                        // Размер вектора инициализации (IV) в байтах
    private static final int TAG_LENGTH_BIT = 128;                // Длина тега аутентификации (MAC) в битах
    private final SecretKey secretKey;                            // Секретный ключ для шифрования/дешифрования
    private final SecureRandom secureRandom = new SecureRandom(); // Криптографически стойкий генератор случайных чисел

     // Конструктор инициализирует шифратор с помощью переданного секретного ключа
    public AesGcmEncryptor(byte[] keyBytes) {
        if (keyBytes == null || (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32)) {
            throw new IllegalArgumentException("Key must be 16/24/32 bytes for AES-128/192/256");
        }
        // Обертываем сырые байты в объект SecretKey, специфичный для AES
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    // Шифрует исходный текст
    // Параметр plainText - исходная текстовая строка, которую нужно засекретить
    public String encrypt(String plainText) {
        try {
            // Генерация случайного уникального IV для текущей сессии шифрования
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);

            // Инициализация криптографического шифра
            Cipher cipher = Cipher.getInstance(ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            // Шифрование текста (Java автоматически добавит 16-байтовый тег аутентификации в конец cipherText)
            byte[] cipherText = cipher.doFinal(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Совмещение IV и cipherText в единый буфер для удобства хранения
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            byte[] cipherMessage = byteBuffer.array();

            // Кодирование бинарных данных в безопасную для передачи строку текста
            return Base64.getEncoder().encodeToString(cipherMessage);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    //Дешифрование строки, раннее зашифрованной в этм классе
    public String decrypt(String base64IvCiphertext) {
        try {
            // Декодирование строки Base64 обратно в бинарный массив байт
            byte[] decoded = Base64.getDecoder().decode(base64IvCiphertext);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            // Извлечение первых 12 байт - вектора инициализации (IV)
            byte[] iv = new byte[IV_SIZE];
            byteBuffer.get(iv);

            // Все оставшиеся байты - это сам зашифрованный текст вместе с тегом проверки
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            // Инициализация шифра в режиме дешифрования (DECRYPT_MODE)
            Cipher cipher = Cipher.getInstance(ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            // Само дешифрование
            // На этом этапе Java автоматически проверяет тег целостности
            // Если данные менялись — упадет Exception.
            byte[] plain = cipher.doFinal(cipherText);

            // Превращение байт оригинального текста обратно в String
            return new String(plain, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}