import java.net.*;
import java.io.*;

public class BackendServer {
    
    static ServerSocket servSocket;
    static Socket client;

    public static void main(String[] args) throws Exception{

        NameDatabase.populate();
        Ledger.populate();

        servSocket = new ServerSocket(5001); //at Port 5001
        while(true){

            System.out.println("Waiting for Requests ...\n");
            client = servSocket.accept();

            int len = client.getInputStream().read();

            byte[] b = new byte[len];
            String temp = "";
            client.getInputStream().read(b);
            
            for(int i=0;i<b.length;i++)
                temp += (char) b[i];
            
            String Sender = temp;
            temp = "" + Ledger.getBalance(Sender);

            client.getOutputStream().write(temp.length());
            client.getOutputStream().write(temp.getBytes());

            len = client.getInputStream().read();

            if(len == -1)
                continue ;

            b = new byte[len];
            temp = "";
            client.getInputStream().read(b);
            
            for(int i=0;i<b.length;i++)
                temp += (char) b[i];
            
            String Receiver = temp.substring(0,4);
            float money = Float.parseFloat(temp.substring(4));
            Ledger.addToTable(Sender,Receiver,money);
        }
    }

}

class NameDatabase{

    static String[] userID = new String[100];
    static String[] username = new String[100];

    static void populate() throws Exception{ //Populate the Username and UserID table

        FileInputStream fin = new FileInputStream(new File("NameDB.txt"));

        int b;
        String contents = "";
        while((b = fin.read()) != -1)
            contents += (char) b;
        
        String[] tempSplitted = contents.split("\n");

        for(int i=0;i<tempSplitted.length;i++){

            String[] moreSplit = tempSplitted[i].split(" ");

            userID[i] = moreSplit[0];
            username[i] = moreSplit[1];
        }
    }   

    
}

class Ledger{

    static String[] SenderID = new String[100];
    static String[] ReceiverID = new String[100];
    static float[] siphonsSpent = new float[100];
    static int index = 0;

    public static void populate() throws Exception{

        FileInputStream fin = new FileInputStream("Ledger.txt");

        int b;
        String contents = "";
        while((b = fin.read()) != -1)
            contents += (char) b;

        String[] tempSplitted = contents.split("\n");

        for(int i=0;i<tempSplitted.length;i++){
    
            String[] moreSplit = tempSplitted[i].split(" ");

            if(moreSplit.length < 3)
                break;

            SenderID[i] = moreSplit[0];
            ReceiverID[i] = moreSplit[1];
            siphonsSpent[i] = Float.parseFloat(moreSplit[2]);

            index++;
        }

        fin.close();
    }

    static float getBalance(String userID){

        float balance = 0f;
        for(int i=0;i<index;i++){

            if(SenderID[i].equals(userID))
                balance -= siphonsSpent[i];
            else if(ReceiverID[i].equals(userID))
                balance += siphonsSpent[i];

        }

        return balance;
    }

    static void addToTable(String s,String r,float amount) throws Exception{

        SenderID[index] = s;
        ReceiverID[index] = r;
        siphonsSpent[index] = amount;

        index++;

        //Adding to the File

        String toWrite = "\n" + s + " " + r + " " + amount;
        FileOutputStream fout = new FileOutputStream(new File("Ledger.txt"),true);
        fout.write(toWrite.getBytes());

        fout.close();
    }
}
