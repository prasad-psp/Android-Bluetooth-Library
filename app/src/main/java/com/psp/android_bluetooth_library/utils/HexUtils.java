package com.psp.android_bluetooth_library.utils;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;

import androidx.annotation.ColorInt;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public class HexUtils {

    @ColorInt
    static int caretBackground = 0xff666666;


    public static boolean isValidHexString(String hex) {
        try {
            hex = hex.replace(" ","");
            return hex.matches("^[0-9A-Fa-f]+$");
        } catch (Exception e) {
            return false;
        }
    }

    public static CharSequence toCaretString(CharSequence s, boolean keepNewline) {
        return toCaretString(s, keepNewline, s.length());
    }

    public static CharSequence toCaretString(CharSequence s, boolean keepNewline, int length) {
        boolean found = false;
        for (int pos = 0; pos < length; pos++) {
            if (s.charAt(pos) < 32 && (!keepNewline ||s.charAt(pos)!='\n')) {
                found = true;
                break;
            }
        }
        if(!found)
            return s;
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for(int pos=0; pos<length; pos++)
            if (s.charAt(pos) < 32 && (!keepNewline ||s.charAt(pos)!='\n')) {
                sb.append('^');
                sb.append((char)(s.charAt(pos) + 64));
                sb.setSpan(new BackgroundColorSpan(caretBackground), sb.length()-2, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                sb.append(s.charAt(pos));
            }
        return sb;
    }

  /*  public static String convertStringToFormattedHex(String str) {
        StringBuilder hex = new StringBuilder();

        for (char ch : str.toCharArray()) {
            hex.append(String.format("%02x", (int) ch));
        }

        return formatHexWithSpaces(hex.toString());
    }

    public static String formatHexWithSpaces(String hex) {
        StringBuilder spacedHex = new StringBuilder();

        for (int i = 0; i < hex.length(); i += 2) {
            spacedHex.append(hex.substring(i, i + 2));

            // Add a space after every two characters, except at the end
            if (i < hex.length() - 2) {
                spacedHex.append(" ");
            }
        }

        return spacedHex.toString();
    }*/

    public static byte[] filterNonZeroBytes(byte[] buffer) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (byte b : buffer) {
            if (b != 0x00) {
                outputStream.write(b);
            }
        }

        return outputStream.toByteArray();
    }

    public static String convertBytesToFormattedHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();

        for (byte b : bytes) {
            hex.append(String.format("%02x", b)).append(" ");
        }

        return hex.toString().trim();
    }


    public static String convertStringToFormattedHex(String str, String charsetName) {
        Charset charset = Charset.forName(charsetName);
        byte[] bytes = str.getBytes(charset);
        StringBuilder hex = new StringBuilder();

        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }

        return formatHexWithSpaces(hex.toString());
    }

    public static String formatHexWithSpaces(String hex) {
        StringBuilder spacedHex = new StringBuilder();

        for (int i = 0; i < hex.length(); i += 2) {
            spacedHex.append(hex.substring(i, i + 2));

            // Add a space after every two characters, except at the end
            if (i < hex.length() - 2) {
                spacedHex.append(" ");
            }
        }

        return spacedHex.toString();
    }

    public static byte[] hexStringToByteArray(String str) {
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4)
                    + Character.digit(str.charAt(i+1), 16));
        }
        return data;
    }


    public static byte[] getHexBytesFromString(String str) {
        try {
            String hexString = convertToHexString(str);
            return  hexStringToByteArray(hexString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String convertToHexString(String hexInput) {
        // Split the input string by spaces
        String[] hexValues = hexInput.split("\\s+");

        StringBuilder hexString = new StringBuilder();
        for (String hex : hexValues) {
            // Ensure each hex value is two digits by padding with leading zero if necessary
            hexString.append(String.format("%02X", Integer.parseInt(hex, 16)));
        }

        return hexString.toString();
    }
}
