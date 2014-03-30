/***
 * Copyright (c) 2008, Endless Loop Software, Inc.
 * 
 * This file is part of EgoNet.
 * 
 * EgoNet is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EgoNet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.egonet.util;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author admin
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AsymmetricEncryption
{
	final private static Logger logger = LoggerFactory.getLogger(AsymmetricEncryption.class);
	
   private PrivateKey privateKey;
   private PublicKey  publicKey;

   public void generateKeys()
   {
      try
      {
         KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
         keyGen.initialize(1024);
         KeyPair pair = keyGen.generateKeyPair();
         this.privateKey = pair.getPrivate();
         this.publicKey = pair.getPublic();
         logger.info("Public key: " + getString(publicKey.getEncoded()));
         logger.info("Private key: " + getString(privateKey.getEncoded()));
      }
      catch (Exception ex)
      {
         logger.error(ex.toString());
      }
   }

   public String sign(String plaintext)
   {
      try
      {
         Signature dsa = Signature.getInstance("SHA1withDSA");
         dsa.initSign(privateKey);
         dsa.update(plaintext.getBytes());
         byte[] signature = dsa.sign();
         return getString(signature);
      }
      catch (Exception ex)
      {
         logger.error(ex.toString());
      }
      return null;
   }

   public boolean verifySignature(String plaintext, String signature)
   {
      try
      {
         Signature dsa = Signature.getInstance("SHA1withDSA");
         dsa.initVerify(publicKey);

         dsa.update(plaintext.getBytes());
         boolean verifies = dsa.verify(getBytes(signature));
         logger.info("signature verifies: " + verifies);
         return verifies;
      }
      catch (Exception ex)
      {
         logger.error(ex.toString());
      }
      return false;
   }

   /**
    * Returns true if the specified text is encrypted, false otherwise
    */
   public static boolean isEncrypted(String text)
   {
      // If the string does not have any separators then it is not
      // encrypted
      if (text.indexOf('-') == -1) {
      ///logger.info( "text is not encrypted: no dashes" );
      return false; }

      StringTokenizer st = new StringTokenizer(text, "-", false);
      while (st.hasMoreTokens())
      {
         String token = st.nextToken();
         if (token.length() > 3) { return false; }
         for (int i = 0; i < token.length(); i++)
         {
            if (!Character.isDigit(token.charAt(i))) { return false; }
         }
      }
      //logger.info( "text is encrypted" );
      return true;
   }

   private static String getString(byte[] bytes)
   {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < bytes.length; i++)
      {
         byte b = bytes[i];
         sb.append((int) (0x00FF & b));
         if (i + 1 < bytes.length)
         {
            sb.append("-");
         }
      }
      return sb.toString();
   }

   private static byte[] getBytes(String str)
   {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      StringTokenizer st = new StringTokenizer(str, "-", false);
      while (st.hasMoreTokens())
      {
         int i = Integer.parseInt(st.nextToken());
         bos.write((byte) i);
      }
      return bos.toByteArray();
   }

   public static void main(String[] args)
   {
      AsymmetricEncryption pki = new AsymmetricEncryption();
      pki.generateKeys();
      String data = "This is a test";
      String baddata = "This is an test";
      String signature = pki.sign(data);
      String badSignature = signature.substring(0, signature.length() - 1) + "1";
      boolean verifies = pki.verifySignature(data, signature);
      boolean verifiesBad = pki.verifySignature(data, badSignature);
      boolean verifiesBad2 = pki.verifySignature(baddata, signature);

      logger.info("Data: " + data);
      logger.info("Signature: " + signature);
      logger.info("Verifies (good): " + verifies);
      logger.info("Bad Signature: " + badSignature);
      logger.info("Verifies (bad): " + verifiesBad);
      logger.info("Verifies (bad2): " + verifiesBad2);
   }
}

