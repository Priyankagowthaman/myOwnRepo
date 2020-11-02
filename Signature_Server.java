import java.util.*;
import java.io.*;
import java.net.*;

//Public Key Private Key set
class User_signature {
	private int private_key;
	public String public_key;

	User_signature(String public_key){
		Random R=new Random();
		this.public_key=public_key;
		this.private_key=R.nextInt(128);
	}

	User_signature(String public_key,int private_key){
		this.private_key=private_key;
		this.public_key=public_key;
		
	}

	//Generate Random key
	void update(){
		Random R=new Random();
		this.private_key=R.nextInt(128);
	}
	//Get private key of a user
	int get_key(){
		return this.private_key;
	}

	
}

//Set of all public key - private key pairs
class Signature_set{
	private List<User_signature> list=new ArrayList<User_signature>();  
	
	//Load from file
	Signature_set(){
		try {
           
            File F = new File("Signature_set.txt");	
            Scanner SC = new Scanner(F);
            while(SC.hasNextLine()){
            		String temp=SC.nextLine();
					String input[]=temp.split(" ",2);	
					this.list.add(new User_signature(input[0],Integer.parseInt(input[1])));	
            	}	
			SC.close();
         
        } 
		catch (FileNotFoundException i) {
           i.printStackTrace();
           return;
        }
        
	}
	//Add a new Public key-Private key pair
	void add_key(String public_key){
		User_signature temp=new User_signature(public_key);
		this.list.add(temp);
	}

	//Upadte existing pair with known public key
	int update(String public_key){
		for(User_signature a:this.list){
			if(a.public_key.equals(public_key)){
				a.update();
				return a.get_key();
			}
		}
		return -1;        	   
	}
	//Get private key of a particular Public key
	int get_key(String public_key){
		for(User_signature a:this.list){
			if(a.public_key.equals(public_key)){
				return a.get_key();
			}
		}
		return 0;        	   
	}

	//Uploading the set to the file
	void upload(){
		try {    
			FileWriter F =new FileWriter("Signature_set.txt");
			F.flush();
            BufferedWriter B = new BufferedWriter(F);
            for(User_signature a:this.list){
				B.write(a.public_key+" "+a.get_key()+"\n");
			}
			B.close();
		}	
        catch (IOException i) {
                i.printStackTrace();
                return;
			 }
	
	}

	void print(){
		for(User_signature a:this.list){
			System.out.println(a.public_key+"  "+a.get_key());
		}
	}

}


public class Signature_Server{
	public static void main(String[] args) throws Exception{

		
		ServerSocket Server=new ServerSocket(8002);
		Signature_set S = new Signature_set();

		while(true){

			System.out.println("\n\n\n\n -- SIPHON SIGNATURE SERVER - ALPHA BUILD -- \n|\n|");
			System.out.println("|    Waiting for Transaction Server Request...");
			Socket R= Server.accept();	

			byte b[] = new byte[5120];
        	R.getInputStream().read(b);
			
			
			String public_key = new String(b).substring(0,4);
			System.out.println("|    Request Received for User : " + public_key);
			System.out.println("|    Sending Back OLD and UPDATED keys...");
			String result=S.get_key(public_key)+" "+S.update(public_key);
			
			R.getOutputStream().write(result.getBytes());
			S.upload();

			System.out.println("|    Ending Current Session...");
		}

	}
}