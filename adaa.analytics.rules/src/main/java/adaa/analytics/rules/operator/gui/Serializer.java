package adaa.analytics.rules.operator.gui;

import java.io.*;
import java.util.Base64;

public class Serializer {
	/***
	 * Deserializes object from Base64 string.
	 * 
	 * @param s String representation.
	 * @return Deserialized object.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object fromString( String s ) throws IOException , ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream( 
                                        new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
   }

    /***
     * Serializes object to Base64 string. 
     * 
     * @param Object to be serialized.
     * @return String representation.
     * @throws IOException
     */
	public static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray()); 
    }
}
