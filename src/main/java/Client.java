import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        boolean stopped = false, hasFoundServer = false;
        String messageOut;
        String messageIn;
        Socket socket = null;
        System.out.println("Recherche d'un serveur...");

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        while (!hasFoundServer) {
            try {
                socket = new Socket("localhost", 22222);
                hasFoundServer = true;
            } catch (IOException ignored) {
            }
        }
        System.out.println("Server found !");

        PrintWriter outs = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        BufferedReader ins = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        Scanner scanner = new Scanner(System.in);

        Cipher ci = Cipher.getInstance("RSA");
        ci.init(Cipher.DECRYPT_MODE, privateKey);

        outs.println(new String(publicKey.getEncoded()));

        Key secretKey = new SecretKeySpec(ci.doFinal(ins.readLine().getBytes()), "DES");
        Cipher cipher = Cipher.getInstance("DES");

        while (!stopped) {
            messageOut = scanner.nextLine();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            messageOut = new String(cipher.doFinal(messageOut.getBytes()));
            outs.println(messageOut);
            messageIn = ins.readLine();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            messageIn = new String(cipher.doFinal(messageIn.getBytes()));
            System.out.println(messageIn);
            if (messageIn.equals("stop") || messageOut.equals("stop")) {
                stopped = true;
            }
        }
        System.out.println("Client termination");
    }
}
