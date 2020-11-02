import java.util.*;
import java.io.*;
import java.net.*;

public class TransactionControlServer {
    
    static Scanner sc = new Scanner(System.in);
    static boolean isSender;
    static String User1,User2;
    static float money;
    static String transactionDetails,transactionName;
    static String buffer[] = new String[10];
    static Transaction[] bufferedObjects = new Transaction[10];
    static int index = 0;

    public static void main(String[] args) throws Exception{

        
        
        ServerSocket Soc = new ServerSocket(3500);
        while(true){

            System.out.println("\n\n\n\n -- SIPHON TRANSACTION SERVER - ALPHA BUILD -- \n|\n|");
            //Add Sockets here instead of getFile()
        
            System.out.println("|    Waiting for Clients..."); //.
            Socket SocA = Soc.accept();
                  
            System.out.println("|    Connected to a Client, Getting Transaction Copy...");


            byte[] b = new byte[1000];
            int len = SocA.getInputStream().read(b);
            String fileContents = "";

            for(int i = 0;i<len;i++)
                fileContents += (char) b[i];
            
    		
            parseFile(fileContents);
         
            
            Transaction t = new Transaction(User1,User2,money);
            t.name = transactionName;
            t.isSender = isSender;
            t.Decrypt(transactionDetails);
            money = t.Siphons;

            //Validate Signature here! ..
            if(!SignatureValidation(t)){
                System.out.println("|    Signature Mismatch, Transaction Failed!");
                System.out.println("|    Sending New Dynamic Key to User...");
                sendKey(t.dynamicKey,SocA);
                continue;
            }

            System.out.println("|    Sending New Dynamic Key to User...");
            sendKey(t.dynamicKey,SocA);
            int f = checkBuffer(t);
            if(f == -1)
                addToBuffer(t); //Adding to Buffer
            else{

                //Already Got one copy in buffer so do validation for both now
                
                //Do All Validations

                System.out.println("|    Checking Copy for Tamper...");
                boolean isValid = bufferedObjects[f].name.equals(t.name);
                isValid &= ( bufferedObjects[f].isSender !=  t.isSender );
                isValid &= ( bufferedObjects[f].staticID.equals(t.staticID2) );
                isValid &= ( bufferedObjects[f].staticID2.equals(t.staticID) );
                isValid &= ( bufferedObjects[f].oppID.equals(t.ID) );
                isValid &= ( bufferedObjects[f].ID.equals(t.oppID) );
                isValid &= ( bufferedObjects[f].Siphons == t.Siphons );

                if(isValid)
                    addToDatabase(t);
                
                //Not Valid Do Nothing
            }

         
         //Please add that new Signature here (so inspite of outcome send new dynamic key)
        }
    }
    public static Boolean SignatureValidation(Transaction T) throws Exception{

        System.out.println("|    Connected to Signature Server");    
        
        //Sending request to Signature Server
        Socket R = new Socket("localhost",8002);
        R.getOutputStream().write((T.staticID2).getBytes());
        byte b[] = new byte[5120];
        R.getInputStream().read(b);
        R.close();
        String contents = new String(b);
        
        //input[0]- Old Private Key
        //input[1]- New Private Key
		String input[]=contents.split(" ",2);

        System.out.println("|    Validating Signatures ...");
        if(input[0].equals(String.valueOf(Integer.parseInt(T.ID)))){
            T.dynamicKey=input[1];
            return true;
        }
        T.dynamicKey=input[1];    
        return false;
        //sending dyanamic key to enduser
    }
   public static void sendKey(String dynamicKey,Socket SocA) throws Exception
   {
        byte b1[]=(dynamicKey).getBytes();
        SocA.getOutputStream().write(b1);
        SocA.close();
   }
    public static void addToBuffer(Transaction t){

        String checkString = t.name + t.oppID + t.ID + t.transactionDetails;
        buffer[index] = checkString;

        System.out.println("|    Asymmetric copy not found, Adding To Buffer...");
        bufferedObjects[index++] = t;
    }

    public static int checkBuffer(Transaction t){

        String checkString = t.name + t.ID + t.oppID + t.transactionDetails;

        for(int i=0;i<index;i++)
            if(buffer[i].equals(checkString))
                return i;

        return -1;
    }

    public static void addToDatabase(Transaction t) throws Exception{

        Socket server = new Socket("localhost",5001);

        System.out.println("|    Connected to Back-END Server");
        String temp = "";
        
        if(isSender)
            temp += t.staticID;
        else
            temp += t.staticID2;


        server.getOutputStream().write(temp.length());
        server.getOutputStream().write(temp.getBytes());
        
        int len = server.getInputStream().read();
        byte[] b = new byte[len];
        server.getInputStream().read(b);

        float balance = Float.parseFloat(new String(b));

        if(balance < t.Siphons){
            System.out.println("|    Transaction Failure, Not Enough Siphons!");
            server.close();
            return ;
        }

        //Transaction is Valid , add To Ledger
        if(isSender)
            temp = t.staticID2;
        else
            temp = t.staticID;
        
        
        System.out.println("|    Transaction Success, Committing changes to Ledger...");
        temp += t.Siphons;
        server.getOutputStream().write(temp.length());
        server.getOutputStream().write(temp.getBytes());

    }

    /*static void getFile() throws Exception{

        System.out.print("\nEnter the FileName : ");
        String fileName = sc.nextLine();

        FileInputStream fin = new FileInputStream(new File(fileName));
        
        int b;
        String fileContents = "";

        while((b = fin.read()) != -1)
            fileContents += (char) b;
        
        parseFile(fileContents);

    }*/

    static void parseFile(String x){

        isSender = (x.charAt(0) == '1');

        int start = 1;
        transactionName = x.substring(start,start + 4);
        start += 4;

        User1 = x.substring(start,start + 128);
        start += 128;

        User2 = x.substring(start,start + 128);
        start += 128;

        transactionDetails = x.substring(start);

    }
}

class Transaction{

    boolean isSender;
    String name;
    String ID;
    String oppID; //Opposite End User ID

    String staticID;
    String staticID2;
    float Siphons;
    String transactionDetails;
    String dynamicKey;

    Transaction(String a,String b,float money){

        this.ID = a;
        this.oppID = b;
        this.Siphons = money;
    }

    public void Decrypt(String text){


        int in = 0;
        for(int i=0;i<text.length() - 1;i++)
            if(text.charAt(i) == '0' && text.charAt(i + 1) == '0')
                continue;
            else{
                in = i + 1;
                break;
            }

        text = text.substring(in);

        int key1 = Integer.parseInt(ID);
        int key2 = Integer.parseInt(oppID);

        String temp = "";
        for(int i=0;i<text.length();i++){

            int DecryptedText =  (text.charAt(i) - key2) < 0 ? 128 + text.charAt(i) - key2 : (text.charAt(i) - key2);
            DecryptedText =  (DecryptedText - key1) < 0 ? 128 + DecryptedText - key1 : (DecryptedText - key1);

            temp += (char) DecryptedText;
        }

        staticID = temp.substring(0,4);
        staticID2 = temp.substring(6,10);
       
        Siphons = Float.parseFloat(temp.substring(10,12));
    }
}