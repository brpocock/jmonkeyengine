/**
 * UpAxisType.java This file was generated by XMLSpy 2006sp2 Enterprise Edition.
 * YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE OVERWRITTEN WHEN YOU
 * RE-RUN CODE GENERATION. Refer to the XMLSpy Documentation for further
 * details. http://www.altova.com/xmlspy
 */

package com.jmex.model.collada.schema;

import com.jmex.model.collada.types.SchemaString;

public class UpAxisType extends SchemaString {

    private static final long serialVersionUID = 1L;
    public static final int EX_UP = 0; /* X_UP */
    public static final int EY_UP = 1; /* Y_UP */
    public static final int EZ_UP = 2; /* Z_UP */

    public static String[] sEnumValues = { "X_UP", "Y_UP", "Z_UP", };

    public UpAxisType() {
        super();
    }

    public UpAxisType(String newValue) {
        super(newValue);
        validate();
    }

    public UpAxisType(SchemaString newValue) {
        super(newValue);
        validate();
    }

    public static int getEnumerationCount() {
        return sEnumValues.length;
    }

    public static String getEnumerationValue(int index) {
        return sEnumValues[index];
    }

    public static boolean isValidEnumerationValue(String val) {
        for (int i = 0; i < sEnumValues.length; i++) {
            if (val.equals(sEnumValues[i]))
                return true;
        }
        return false;
    }

    public void validate() {

        if (!isValidEnumerationValue(toString()))
            throw new com.jmex.model.collada.xml.XmlException(
                    "Value of UpAxisType is invalid.");
    }
}
