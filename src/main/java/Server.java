import javax.crypto.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        ServerSocket serverSocket = new ServerSocket(22222);
        Socket socket = serverSocket.accept();
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        Scanner scanner = new Scanner(System.in);

        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        keyGenerator.init(56);
        Key secretKey = keyGenerator.generateKey();

        byte[] ClePubRsa = new byte[1024];
        in.read(ClePubRsa, 0, 1024);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(ClePubRsa));

        Cipher cipherRSA = Cipher.getInstance("RSA");
        cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] CleDesOut = cipherRSA.doFinal(secretKey.getEncoded());

        out.write(CleDesOut);

        Cipher cipherDES = Cipher.getInstance("DES");

        while(true) {
            String messageOut, messageIn;
            byte[] messageInBytes = new byte[128];

            in.read(messageInBytes, 0, 128);
            cipherDES.init(Cipher.DECRYPT_MODE, secretKey);
            messageIn = new String(cipherDES.doFinal(messageInBytes));
            System.out.println(messageIn);

            messageOut = scanner.nextLine();
            cipherDES.init(Cipher.ENCRYPT_MODE, secretKey);
            out.write(cipherDES.doFinal(messageOut.getBytes()));

        }
    }
}
