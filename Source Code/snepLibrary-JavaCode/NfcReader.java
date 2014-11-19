//package it.ismb.snep;

/*
 * ISMB-SNEP: Simple test of IsmbSNEPConnection
 * 
* Copyright (C) 2013  Antonio Lotito <lotito@ismb.it>
 * 
 * This file is part of ISMB-SNEP-JAVA LIBRARY.
 * 
 * ISMB-SNEP-JAVA LIBRARY is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or(at your option) any later version.
 * 
 * ISMB-SNEP-JAVA LIBRARY is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with ISMB-SNEP-JAVA-LIBRARY. If not, see http://www.gnu.org/licenses/.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

@SuppressWarnings("restriction")
public class NfcReader {
	List<CardTerminal> terminals;
	CardTerminal terminal = null;	     
	IsmbSnepConnectionTarget n=null;
	String info="";
	String id_trans="";
	Timer timer;
	
	public static void main(String[] args) {		 		 			
		new NfcReader();								
	}
	
	public NfcReader() {			
		 TerminalFactory factory;
		 	
		 //List all available terminals	 		 
		 try {
			 System.out.println("Get factory");
			 factory = TerminalFactory.getDefault();
			 System.out.println("Get terminals");
			 terminals = factory.terminals().list();          
			 if (terminals.size() == 0) {
				 System.out.println("There are not terminals!");
				 terminals = null;		
				 System.exit(0);
			 }
			 else {
				 terminal=terminals.get(0);
				 System.out.println("Terminal name: "+terminal.getName());
				 System.out.println("Available Commands: receive, send and quit");
			 }	
		} 
		catch (CardException c) {
			System.out.print(c.getMessage());
			terminals = null;	
			System.exit(0);
		}		 
		timer=new Timer();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line = "";

		while (line.equalsIgnoreCase("quit") == false) {
			try {
				line = in.readLine();
				if(line.compareTo("send")==0){
					setUp();
					n.sendingProcedure();
						
					
					try {
						in.close();								
					} catch (IOException e) { e.printStackTrace();}	
					System.exit(0);
				}
				else{	
					if(line.compareTo("receive")==0){							
						setUp();						
						n.receivingProcedure();
							
						try {
							in.close();									
						} catch (IOException e) {e.printStackTrace();}	
						System.exit(0);							
					}
					else{
						System.out.println("Command unknown!");
						System.out.println("Supported commands are: send, receive and quit ");
					}	
				}
			} catch (IOException e) { e.printStackTrace();}			      			    
		}
			  
	}
	
	
	public boolean setUp(){
		if (terminal == null)
			terminal=terminals.get(0);
		boolean retVal=true;
		try {                							
			n = new IsmbSnepConnectionTarget(terminal);			
			n.setDebugMode();		
		}	
		catch (IsmbSnepException e) {
			retVal=false;					 
			System.out.println(e.getMessage());	 		 		 
		}
		return retVal;
	}

}
