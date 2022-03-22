import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        boolean stopped = false;
        ServerSocket serverSocket = new ServerSocket(22222);
        Socket socket = serverSocket.accept();
        BufferedReader ins = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        PrintWriter outs = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        keyGenerator.init(56);
        Key secretKey = keyGenerator.generateKey();

        char[] characters = new char[1024];
        int pos = 0;
        do {
            pos += ins.read(characters);
        } while (pos < 1024);
        String receivedKey = new String(characters);
        System.out.println(receivedKey);
        Key clientKey = new SecretKeySpec(receivedKey.getBytes(StandardCharsets.UTF_8), "RSA");
        Cipher ci = Cipher.getInstance("RSA");
        ci.init(Cipher.ENCRYPT_MODE , clientKey);
        byte[] keyToSend = secretKey.toString().getBytes();
        outs.println(new String(ci.doFinal(keyToSend)));

        Scanner scanner = new Scanner(System.in);

        String messageIn;
        String messageOut;
        Cipher cipher = Cipher.getInstance("DES");

        while (!stopped) {
            messageIn = ins.readLine();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            messageIn = new String(cipher.doFinal(messageIn.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            messageOut = scanner.nextLine();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            messageOut = new String(cipher.doFinal(messageOut.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            outs.println(messageOut);
            outs.println(messageOut);
            if (messageIn.equals("stop") || messageOut.equals("stop")) {
                stopped = true;
            }
        }

        ins.close();
        outs.close();
        socket.close();
    }
}