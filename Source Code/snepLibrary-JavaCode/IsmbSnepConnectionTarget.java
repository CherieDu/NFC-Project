/*
 * IsmbSnepConnectionTarget
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


//package it.ismb.snep;


import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;


@SuppressWarnings("restriction")
public class IsmbSnepConnectionTarget {
        	
        //USEFUL APDU COMMANDS 
        private final static byte TG_GET_DATA = (byte) 0x86;
        private final static byte TG_INIT_AS_TARGET = (byte) 0x8c;
        private final static byte TG_SET_DATA = (byte) 0x8e;

        private CardTerminal terminal;
        private CardChannel ch;
        private Card card;
        //  Enable debugMode to print info about the communication
        private boolean debugMode = false;
        
        //Set debug mode
        public void setDebugMode() {
                debugMode = true;
        }
        
		//Unset debug mode
        public void unsetDebugMode() {
                debugMode = false;
        }
        
        
        /**
         * Initialize SNEP Connection
         *
         * @param t
         *            a valid card terminal
         *
         * @throws IsmbSnepException
         *             if the terminal is incorrect
         */
        public IsmbSnepConnectionTarget(CardTerminal t) throws IsmbSnepException {
            if (t == null) {
                    throw new IsmbSnepException("invalid card terminal");
            }
            terminal = t;                
            try {
                 if (terminal.isCardPresent()) {                    	 
                	 card = terminal.connect("*");                    	 
                	 System.out.println("card: "+card);
                     ch = card.getBasicChannel();                   
                     System.out.println("Protocol:"+card.getProtocol());   
                  	 } 
                 else {
                	 throw new IsmbSnepException("Device not supported, only ACS ACR122 is supported now");
                 }
            } catch (CardException e) { throw new IsmbSnepException("problem with connecting to reader");}       
        }
                
        /**
         * Sends and receives APDUs to and from the controller
         *
         * @param instr
         *            Instruction
         * @param param
         *            Payload to send
         *
         * @return The response payload 
         */
        private byte[] transceive(byte instr, byte[] payload) throws IsmbSnepException {
            
                if (ch == null) {
                        throw new IsmbSnepException("channel not open");
                }
                int payloadLength = (payload != null) ? payload.length : 0;
                byte[] instruction = { (byte) 0xd4, instr };

                //ACR122 header
                byte[] header = { (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) (instruction.length + payloadLength) };

                /* construct the command */
                byte[] cmd = Util.appendToByteArray(header, instruction, 0,
                                instruction.length);
                cmd = Util.appendToByteArray(cmd, payload);

                if (debugMode)
                        Util.debugAPDUs(cmd, null);

                try {
                        CommandAPDU c = new CommandAPDU(cmd);
                        ResponseAPDU r = ch.transmit(c);
                        
                        byte[] ra = r.getBytes();

                        if (debugMode)
                                Util.debugAPDUs(null, ra);

                        /* check whether APDU command was accepted by the Controller */
                        if (r.getSW1() == 0x63 && r.getSW2() == 0x27) {
                                throw new CardException(
                                                "wrong checksum from contactless response");
                        } else if (r.getSW1() == 0x63 && r.getSW2() == 0x7f) {
                                throw new CardException("wrong PN53x command");
                        } else if (r.getSW1() != 0x90 && r.getSW2() != 0x00) {
                                throw new CardException("unknown error");
                        }
                        return Util.subByteArray(ra, 2, ra.length - 4);
                } catch (CardException e) {
                		
                        throw new IsmbSnepException("problem with transmitting data");
                }
        }
        
  

        
        public void sendingProcedure(){
        	System.out.println("Called Procedure to Send data .. TARGET MODE");         
 
        	try {	
       	 	 //TG_INIT_AS_TARGET
       		 byte[] targetPayload = { 	
       				(byte) 0x00,   //MODE
	       			//(byte) 0x01, //PASSIVE MODE ONLY 
	       			(byte) 0x00, (byte) 0x00, (byte) 0x00, //MIFARE PARAMS 
	       			(byte) 0x00, (byte) 0x00, (byte) 0x40, //MIFARE PARAMS
	       			(byte) 0x01, (byte) 0xfe, (byte) 0x0f, (byte) 0xbb, (byte) 0xba, (byte) 0xa6, //FELICA PARAMS 
	       			(byte) 0xc9, (byte) 0x89, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, //FELICA PARAMS
	       			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, //FELICA PARAMS
	       			(byte) 0x01, (byte) 0xfe, (byte) 0x0f, (byte) 0xbb, (byte) 0xba, //NFCID3t
	       			(byte) 0xa6, (byte) 0xc9, (byte) 0x89, (byte) 0x00, (byte) 0x00, //NFCID3t	
	       			(byte) 0x0F, //LEN Gt
	       			(byte) 0x46, (byte) 0x66, (byte) 0x6D, //LLCP WORD 	       			
	       			(byte) 0x01, (byte) 0x01, (byte) 0x10, //VERSION NUMBER
	       			(byte) 0x03, (byte) 0x02, (byte) 0x00, (byte) 0x01, //WELL KNOWN SERVICE LIST
	       			(byte) 0x04, (byte) 0x01, (byte) 0x96 //LINK TIMEOUT
      		 	 };       			
      		 	transceive(TG_INIT_AS_TARGET, targetPayload);
			} catch (IsmbSnepException e) {e.printStackTrace();}      
        	
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			
			try { 
				 //GETDATA
		 		 transceive(TG_GET_DATA, null);
		 	} catch (IsmbSnepException e) {e.printStackTrace();}	
		 			 	
		    try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}

			try { 
		 		byte[] targetPayload = { 
			 				(byte)0x11, (byte)0x20,};	
		 		transceive(TG_SET_DATA, targetPayload);
			 } catch (IsmbSnepException e) {e.printStackTrace();}
		 	
		    try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			 	
			try { 
				byte[] targetPayload = { 
			 				(byte)0x00, (byte)0x00,};	
			 	transceive(TG_SET_DATA, targetPayload);
			 } catch (IsmbSnepException e) {e.printStackTrace();}
			 	
			try {
				 Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
					
			try { 				 		
				//GETDATA
				transceive(TG_GET_DATA, null);
			} catch (IsmbSnepException e) {e.printStackTrace();}	
					

			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			 						
			try { 
		 		byte[] targetPayload = { 
		 				(byte) 0x13, (byte) 0x20, //INFO LLCP		 
	        			(byte) 0x00, //SEQUENCE
	        			(byte) 0x10, //VERSION			        		
	        			(byte) 0x02, //PUT		        		
	        			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x58, //SNEP LENGTH
	        			(byte)0xD2, //NDEF FIRST BYTE
	        			(byte)0x24, //TYPE LENGTH
	        			(byte)0x31, //PAYLOAD LENGTH
	        			//application/com.example.android.beam	
	        			(byte)0x61, (byte)0x70, (byte)0x70, (byte)0x6C, (byte)0x69, 
	        			(byte)0x63, (byte)0x61,	(byte)0x74, (byte)0x69, (byte)0x6F, 
	        			(byte)0x6E, (byte)0x2F, (byte)0x63, (byte)0x6F, (byte)0x6D, 
	        			(byte)0x2E, (byte)0x65, (byte)0x78, (byte)0x61, (byte)0x6D, 
	        			(byte)0x70, (byte)0x6C,	(byte)0x65, (byte)0x2E, (byte)0x61, 
	        			(byte)0x6E, (byte)0x64, (byte)0x72, (byte)0x6F, (byte)0x69,
	        			(byte)0x64, (byte)0x2E, (byte)0x62, (byte)0x65, (byte)0x61, 
	        			(byte)0x6D, 
	        			//end of application/com.example.android.beam	
	        			(byte)0x42,
	        			(byte)0x65, (byte)0x61, (byte)0x6D, (byte)0x20, //Beam 
	        			(byte)0x72,
	        			(byte)0x65, (byte)0x63, (byte)0x65, (byte)0x69, (byte)0x76,
	        			(byte)0x65, (byte)0x64, (byte)0x20, //received
	        			(byte)0x66, (byte)0x72, (byte)0x6F, (byte)0x6D, (byte)0x20, //from
	        			(byte)0x49, (byte)0x53, (byte)0x4D, (byte)0x42, (byte)0x20, //ISMB
	        			(byte)0x53, (byte)0x4E, (byte)0x45, (byte)0x50, (byte)0x20, //SNEP
	        			(byte)0x4C, (byte)0x49, (byte)0x42,	        			
	        			(byte)0x52, (byte)0x41, (byte)0x52,
	        			(byte)0x59, (byte)0x3A, (byte)0x0A,
	        			(byte)0x0A, //LIBRARY: /n
	        			(byte)0x67, (byte)0x6F,
	        			(byte)0x6F, (byte)0x64, (byte)0x20, //good
	        			(byte)0x6C, (byte)0x75, (byte)0x63,
	        			(byte)0x6B, (byte)0x21 //luck!
		 				};	
				 	transceive(TG_SET_DATA, targetPayload);
			} catch (IsmbSnepException e) {e.printStackTrace();}
				 	
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
				 	
			try { 
				 //GETDATA
		 		 transceive(TG_GET_DATA, null); //RECEIVED RR
		 		 //[DEBUG] {receiving [8 bytes]} 0xD5 0x87 0x00 0x83 0x44 0x01 0x90 0x00 
		 	} catch (IsmbSnepException e) {e.printStackTrace();}
		 
		 	try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
				 	
						
			//INVIO DISC
			try { 
				byte[] targetPayload = { 
			 				(byte) 0x11, (byte) 0x60};	
			 	transceive(TG_SET_DATA, targetPayload);
			 } catch (IsmbSnepException e) {e.printStackTrace();}
			 
			try {
				 Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			 	
			try { 	
				//GETDATA
				transceive(TG_GET_DATA, null); 			 		
			} catch (IsmbSnepException e) {e.printStackTrace();}
        }



        public void receivingProcedure(){
        	System.out.println("Called new Procedure to receive data .. TARGET MODE");         
			 			 	
        	try {	
	       	 	 //TG_INIT_AS_TARGET
	       		 byte[] targetPayload = { 	
	       				//(byte) 0x00,   //MODE
	       				(byte) 0x02,   //DEP ONLY
		       			//(byte) 0x01, //PASSIVE MODE ONLY 
		       			(byte) 0x00, (byte) 0x00, (byte) 0x00, //MIFARE PARAMS 
		       			(byte) 0x00, (byte) 0x00, (byte) 0x40, //MIFARE PARAMS
		       			(byte) 0x01, (byte) 0xfe, (byte) 0x0f, (byte) 0xbb, (byte) 0xba, (byte) 0xa6, //FELICA PARAMS 
		       			(byte) 0xc9, (byte) 0x89, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, //FELICA PARAMS
		       			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, //FELICA PARAMS
		       			(byte) 0x01, (byte) 0xfe, (byte) 0x0f, (byte) 0xbb, (byte) 0xba, //NFCID3t
		       			(byte) 0xa6, (byte) 0xc9, (byte) 0x89, (byte) 0x00, (byte) 0x00, //NFCID3t	       		
		       			(byte) 0x0F, //LEN Gt
		       			(byte) 0x46, (byte) 0x66, (byte) 0x6D, //LLCP WORD 
		       			(byte) 0x01, (byte) 0x01, (byte) 0x10, //VERSION NUMBER
		       			(byte) 0x03, (byte) 0x02, (byte) 0x00, (byte) 0x01, //WELL KNOWN SERVICE LIST
		       			(byte) 0x04, (byte) 0x01, (byte) 0x96 //LINK TIMEOUT
	      		 	 };       			
	      		 transceive(TG_INIT_AS_TARGET, targetPayload);
			} catch (IsmbSnepException e) {e.printStackTrace();}      
        				
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			
			try { 
				 //GETDATA
		 		 transceive(TG_GET_DATA, null);
		 	} catch (IsmbSnepException e) {e.printStackTrace();}	
					 	
		    try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
		    
			//WAIT FOR A  Connect request    					 				 
			while(true){
				try { 
			 		byte[] targetPayload = { (byte)0x00, (byte)0x00, };	
			 		transceive(TG_SET_DATA, targetPayload);
			 	} catch (IsmbSnepException e) {e.printStackTrace();}
			 	
			 	try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {e1.printStackTrace();}
					
				try { //GETDATA
					byte[]array = transceive(TG_GET_DATA, null);
					
					if ((byte)array[1]==(byte)0x11){			//RECEIVE SOMETHING SIMILAR TO 0x11 0x20		
						break;
					}					
				} catch (IsmbSnepException e) {e.printStackTrace();}	
										
			 	try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {e1.printStackTrace();}				
			}
			//SEND CC - CONNECTION COMPLETE 			 
			try { 
				 byte[] targetPayload = { (byte)0x81, (byte)0x84};	
				 transceive(TG_SET_DATA, targetPayload);
			}catch (IsmbSnepException e) {e.printStackTrace();}
			
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}	

			//WAIT FOR A SNEP REQUEST
			while(true){
				try { 
			 		byte[] targetPayload = { (byte)0x00, (byte)0x00, };	
			 		transceive(TG_SET_DATA, targetPayload);
			 	} catch (IsmbSnepException e) {e.printStackTrace();}
			 	
			 	try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {e1.printStackTrace();}
					
				try { //GETDATA
					byte[]array = transceive(TG_GET_DATA, null);
					if (array.length>5){											
						System.out.println("Something received: \n"+ Util.byteArrayToAsciiString(array));
						break;					
					}
				 } catch (IsmbSnepException e) {e.printStackTrace();}	
										
			 	 try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {e1.printStackTrace();}				
			}			
			
			//SNEP REQUEST RECEIVED
			try { 
			 	byte[] targetPayload = { (byte)0x83, (byte)0x44,(byte)0x01 }; //Receive Ready	
			 	transceive(TG_SET_DATA, targetPayload);
			 } catch (IsmbSnepException e) {e.printStackTrace();}
			 	
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			
			try { //GETDATA
				byte[]array = transceive(TG_GET_DATA, null);										
			} catch (IsmbSnepException e) {e.printStackTrace();}		
							
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
					 
			try { 				 								       
			 	byte[] targetPayload = { (byte)0x83, (byte)0x04,(byte)0x01, (byte)0x10, (byte)0x81,		
			 				(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00 //LENGTH
			 			}; 	
			 	transceive(TG_SET_DATA, targetPayload);
			} catch (IsmbSnepException e) {e.printStackTrace();}		
				
			//WAIT FOR RR
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
				
			try { //GETDATA
				byte[]array = transceive(TG_GET_DATA, null);										
			} catch (IsmbSnepException e) {e.printStackTrace();}
			
			try { 
		 		byte[] targetPayload = { (byte)0x00, (byte)0x00, };	
		 		transceive(TG_SET_DATA, targetPayload);
		 	} catch (IsmbSnepException e) {e.printStackTrace();}
		 	
			try { //GETDATA
				byte[]array = transceive(TG_GET_DATA, null);										
			} catch (IsmbSnepException e) {e.printStackTrace();}
															
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
					
			try { 					 	 
				byte[] targetPayload = { (byte)0x81, (byte)0xC4,(byte)0x00 }; //Disconnected Mode
		 		transceive(TG_SET_DATA, targetPayload);
			} catch (IsmbSnepException e) {e.printStackTrace();}
		
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
				
			try { //GETDATA
				byte[]array = transceive(TG_GET_DATA, null);										
			} catch (IsmbSnepException e) {e.printStackTrace();}
 
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {e1.printStackTrace();}
        }
}
