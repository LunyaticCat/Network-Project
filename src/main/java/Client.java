import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        boolean hasFoundServer = false;
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

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        Scanner scanner = new Scanner(System.in);

        System.out.println("Server found !");

        out.write(publicKey.getEncoded());

        byte[] CleDesIn = new byte[128];

        // Récupère la clé secrète encodé
        in.read(CleDesIn, 0, 128);
        // Décode la clé secrète en utilisant la clé privée
        Cipher cipherRSA = Cipher.getInstance("RSA");
        cipherRSA.init(Cipher.DECRYPT_MODE, privateKey);
        // décode la clé DES, encore sous la forme de tableau de bytes
        byte[] CleDesByte = cipherRSA.doFinal(CleDesIn);
        // transforme le tableau de bytes décode en clé secrète DES
        Key CleDES = new SecretKeySpec(CleDesByte, 0, CleDesByte.length, "DES");

        Cipher cipherDES = Cipher.getInstance("DES");

        while (true) {
            String messageOut, messageIn;
            byte[] messageInBytes = new byte[8];

            messageOut = scanner.nextLine();
            cipherDES.init(Cipher.ENCRYPT_MODE, CleDES);
            out.write(cipherDES.doFinal(messageOut.getBytes()));

            in.read(messageInBytes, 0, 8);
            cipherDES.init(Cipher.DECRYPT_MODE, CleDES);
            messageIn = new String(cipherDES.doFinal(messageInBytes));
            System.out.println(messageIn);
        }
    }
}
