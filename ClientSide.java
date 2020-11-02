import java.util.*;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.*;
class Encryption{
    public String CeaserCipher(int key,String Details)
    {
        //System.out.println(Details);
        String EncryptedDet = "";
        for(int i = 0;i<Details.length();i++)
        {
         char c;   
            if(Details.charAt(i)+key > 127)
            c = (char)((Details.charAt(i)+key)% 128);
            else
            c = (char)(Details.charAt(i)+key);
            EncryptedDet += c;
        }
        return EncryptedDet;
    }
   
}
class Transaction{
    private  String SenderKey;
    private  String ReceiverKey;
    private int Siphons;
    private String TransactionName = "0000";
    private int SenderBit;
    Random num = new Random();
    Transaction(String Sk,String Rk)
    {
        SenderKey = Sk;
        ReceiverKey = Rk;
      
    }

    public void Sender() throws Exception
    {

        Scanner sc = new Scanner(System.in); 
        
        System.out.print("    Enter the Siphons to send : ");
        Siphons = sc.nextInt();
        
        Socket SocA = new Socket("localhost",3456);

        System.out.print("|");
        System.out.println("    Connected to Receiver");
        
        //read the key from the File
        FileInputStream fin=new FileInputStream("dynamickey1.txt");
        int a;
        String s = "";

        while((a = fin.read()) != -1)
            s += (char) a;
        fin.close();
        int Key = Integer.parseInt(s);//UserA key

        System.out.print("|");
        System.out.println("    Encrypting the transaction using your dynamic key ("+ Key +").. ");

        String formattedKey = String.format("%0128d", Key);

        SenderBit = 0;
        String TransactionDet = SenderKey+"->"+ReceiverKey+Siphons; 
        Encryption obj = new Encryption();
        String EncryptedDet = obj.CeaserCipher(Key, TransactionDet);
        String Contents = formattedKey+EncryptedDet+Siphons;

        System.out.print("|");
        System.out.println("    Sending Copy to the Receiver ...");

        byte b[] = Contents.getBytes();
        SocA.getOutputStream().write(b); //sending sender's key and encrypted transaction details to the receiver
        
        byte b1[] = new byte[5120];
        SocA.getInputStream().read(b1); //getting Receiver's key and encrypted transaction details
        SocA.close();

        String UserBDet = new String(b1);

        System.out.print("|");
        System.out.println("    Digitally Signing Receiver Copy using Dynamic Key ...");

        String TwoWayEncrypted = obj.CeaserCipher(Key,UserBDet.substring(128,140));
        String formattedTwoWayEncrypted = String.format("%512s", TwoWayEncrypted).replace(' ','0');
        byte b2[] = (SenderBit+TransactionName+formattedKey+UserBDet.substring(0,128)+formattedTwoWayEncrypted).getBytes();
        
        
        GenerateReceiverCopy(b2);

    }

    public void GenerateReceiverCopy(byte b[]) throws Exception
    {

        
        Socket SocT = new Socket("localhost",3500);
        SocT.getOutputStream().write(b);

        System.out.print("|");
        System.out.println("    Connected to Transaction Control Server and Sending copy ...");

        byte b1[]=new byte[16];
        //get the dynamic key from TransactionControl Server
        SocT.getInputStream().read(b1);
        SocT.close();

        System.out.print("|");
        System.out.println("    Sent Success , Getting new Dynamic Key from Server ...");
        FileOutputStream fout=new FileOutputStream("dynamickey1.txt");

        String s = "";
        for(int i=0;i<b1.length;i++)
            if(b1[i] >= '0' && b[i] <= '9')
                s += (char)b1[i];
            else
                break;
        
        fout.write(s.getBytes());
        fout.close();

        System.out.print("|");
        System.out.println("    Ending Current Session ...");
    }
    public void Receiver() throws Exception{
       
        ServerSocket SocB = new ServerSocket(3456);
        System.out.println("    Waiting for Sender..");
        Socket SocA = SocB.accept();
        System.out.print("|");
        System.out.println("    Sender found");
        byte b[] = new byte[5120];
        
        System.out.print("|");
        System.out.println("    Sending Copy to the Sender ...");
        SocA.getInputStream().read(b); //reading Sender's version of Transaction Details
        String contents = new String(b);
       
        SenderBit = 1;
       //read the key from the File
       FileInputStream fin=new FileInputStream("dynamickey2.txt");
       int a;
        String s = "";

        while((a = fin.read()) != -1)
            s += (char) a;
            fin.close();
        int Key = Integer.parseInt(s);//UserA key
       
        String formattedKey = String.format("%0128d",Key);
        String TransactionDet = ReceiverKey+"->"+SenderKey+contents.substring(140,142);
        
        Encryption obj = new Encryption();
        
        System.out.print("|");
        System.out.println("    Digitally Signing Sender Copy using Dynamic Key ...");
        
        String TwoWayEncrypted  =  obj.CeaserCipher(Key,contents.substring(128,140));
        String formattedTwoWayEncrypted  =  String.format("%512s", TwoWayEncrypted).replace(' ','0');
        String Encrypted = obj.CeaserCipher(Key,TransactionDet);
        
        byte b2[] = (formattedKey+Encrypted).getBytes();
        
        SocA.getOutputStream().write(b2);
        SocA.close();
        SocB.close();
        byte b1[] = (SenderBit+TransactionName+formattedKey+contents.substring(0,128)+formattedTwoWayEncrypted).getBytes();
        GenerateSenderCopy(b1);
        
    }
    public void GenerateSenderCopy(byte b[]) throws Exception
    {
     
        Socket SocT = new Socket("localhost",3500);

        System.out.print("|");
        System.out.println("    Connected to Transaction Control Server and Sending copy ...");

        SocT.getOutputStream().write(b);

        byte b1[]=new byte[16];
        SocT.getInputStream().read(b1);
        SocT.close();

        System.out.print("|");
        System.out.println("    Sent Success , Getting new Dynamic Key from Server ...");

        FileOutputStream fout = new FileOutputStream("dynamickey2.txt");
        String s = "";
        for(int i=0;i<b1.length;i++)
            if(b1[i] >= '0' && b1[i] <= '9')
                s += (char)b1[i];
            else
                break;
        
        fout.write(s.getBytes());
        fout.close();

        System.out.print("|");
        System.out.println("    Ending Current Session ...");
    }
   

}
public class ClientSide{
    public static void main(String args[]) throws Exception
    {
        
        Scanner sc = new Scanner(System.in); 
        while(true)
        {
            System.out.println("\n\n\n\n -- SIPHON MENU - ALPHA BUILD -- \n|\n|");
            System.out.println("|    1 - SEND SIPHONS ");
            System.out.println("|    2 - RECEIVE SIPHONS ");
            System.out.println("|    3 - EXIT ");
            System.out.print("|\n|\n|    Enter your option : ");
        
            int option = sc.nextInt();
            System.out.print("|");

            String SenderKey = "1234"; //static public key
            String ReceiverKey = "4321";
            Transaction obj = new Transaction(SenderKey, ReceiverKey);
            if(option == 1)
                obj.Sender();
            else if(option == 2)
                obj.Receiver();
            else if(option == 3)
                break;
            else
                System.out.println("Invalid option!");
        }
    }
}