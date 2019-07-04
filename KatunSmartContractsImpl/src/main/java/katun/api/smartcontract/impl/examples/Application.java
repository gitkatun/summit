package katun.api.smartcontract.impl.examples;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import katun.api.smartcontract.impl.Service;

public class Application {

	public static void main(String[] args) {
		
		Service svcObj = new Service();
		try {
			svcObj.setUserCredencials("CnZoZF4kPpqUkCvd3bUyee", "4DrcKtFyXpBdokQa29efXfHAJebRfz955UhzVP8z2ApS");
			svcObj.sendKatunTransaction("SenderHash", "ReceiverHash", "Data to be Sent");			
			//svcObj.queryTransactionByHash("55ec603a8bca46adfa51e0238c2e41a9b44a62c2167eb8ba37eec10550d24812");
			//svcObj.createSmartContract(new File("c:\\Transfer.java"));-->creado con este HASH no puede duplicarse por nombre 767373e68257234ff85ed44033d07abd09df078c80a072f6d080c687781b2d63
			//svcObj.queryContractByHash("767373e68257234ff85ed44033d07abd09df078c80a072f6d080c687781b2d63");
			//svcObj.executeContractByHash("767373e68257234ff85ed44033d07abd09df078c80a072f6d080c687781b2d63", 
			//		"function:suma:arg:integer:{10}:arg:integer:{20}:function:getSuccesMessage:" );
		} catch (Exception ex) {
			Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
