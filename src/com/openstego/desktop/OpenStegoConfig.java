/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2017 Samir Vaidya
 */

package com.openstego.desktop;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.openstego.desktop.util.cmd.CmdLineOptions;

/**
 * Class to store configuration data for OpenStego
 */
public class OpenStegoConfig {
    /**
     * Key string for configuration item - useCompression
     * <p>
     * Flag to indicate whether compression should be used or not
     */
    public static final String USE_COMPRESSION_KEY = "useCompression";

    /**
     * Key string for configuration item - useEncryption
     * <p>
     * Flag to indicate whether encryption should be used or not
     */
    public static final String USE_ENCRYPTION_KEY = "useEncryption";

    /**
     * Key string for configuration item - password
     * <p>
     * Password for encryption in case "useEncryption" is set to true
     */
    public static final String PASSWORD_KEY = "password";

    /**
     * Key string for configuration item - encryptionAlgorithm
     * <p>
     * Algorithm to be used for encryption
     */
    public static final String ENCRYPTION_ALGORITHM_KEY = "encryptionAlgorithm";

    /**
     * Value if true
     */
    public static final String TRUE_VALUE = "true";

    /**
     * Value if false
     */
    public static final String FALSE_VALUE = "false";

    /**
     * Flag to indicate whether compression should be used or not
     */
    private boolean useCompression = true;

    /**
     * Flag to indicate whether encryption should be used or not
     */
    private boolean useEncryption = false;

    /**
     * Password for encryption in case "useEncryption" is set to true
     */
    private String password = null;

    /**
     * Algorithm to be used for encryption in case "useEncryption" is set to true
     */
    private String encryptionAlgorithm = OpenStegoCrypto.ALGO_AES128;

    /**
     * Default Constructor (with default values for configuration items)
     */
    public OpenStegoConfig() {
    }

    /**
     * Constructor with map of configuration data. Please make sure that only valid keys for configuration
     * items are provided, and the values for those items are also valid.
     *
     * @param propMap Map containing the configuration data
     * @throws OpenStegoException
     */
    public OpenStegoConfig(Map<String, String> propMap) throws OpenStegoException {
        addProperties(propMap);
    }

    /**
     * Constructor which reads configuration data from the command line options.
     *
     * @param options Command-line options
     * @throws OpenStegoException
     */
    public OpenStegoConfig(CmdLineOptions options) throws OpenStegoException {
        Map<String, String> map = new HashMap<>();

        if (options.getOption("-c") != null) // compress
        {
            map.put(USE_COMPRESSION_KEY, TRUE_VALUE);
        }

        if (options.getOption("-C") != null) // nocompress
        {
            map.put(USE_COMPRESSION_KEY, FALSE_VALUE);
        }

        if (options.getOption("-e") != null) // encrypt
        {
            map.put(USE_ENCRYPTION_KEY, TRUE_VALUE);
        }

        if (options.getOption("-E") != null) // noencrypt
        {
            map.put(USE_ENCRYPTION_KEY, FALSE_VALUE);
        }

        if (options.getOption("-p") != null) // password
        {
            map.put(PASSWORD_KEY, options.getOptionValue("-p"));
        }

        if (options.getOption("-A") != null) // cryptalgo
        {
            map.put(ENCRYPTION_ALGORITHM_KEY, options.getOptionValue("-A"));
        }

        addProperties(map);
    }

    /**
     * Method to add properties from the map to this configuration data
     *
     * @param propMap Map containing the configuration data
     * @throws OpenStegoException
     */
    protected void addProperties(Map<String, String> propMap) throws OpenStegoException {
        Iterator<String> keys = null;
        String key = null;
        String value = null;

        keys = propMap.keySet().iterator();
        while (keys.hasNext()) {
            key = keys.next();
            if (key.equals(USE_COMPRESSION_KEY)) {
                value = propMap.get(key).trim();
                this.useCompression = determineBooleanEquivalent(value, OpenStegoException.INVALID_USE_COMPR_VALUE);
            } else if (key.equals(USE_ENCRYPTION_KEY)) {
                value = propMap.get(key).trim();
                this.useEncryption = determineBooleanEquivalent(value, OpenStegoException.INVALID_USE_ENCRYPT_VALUE);
            } else if (key.equals(PASSWORD_KEY)) {
                this.password = propMap.get(key);
            } else if (key.equals(ENCRYPTION_ALGORITHM_KEY)) {
                this.encryptionAlgorithm = propMap.get(key);
            }
        }
    }

    /**
     * From a string value, determine a boolean equivalent
     * 
     * @param value String containing a boolean as text
     * @param errorCode error code thrown if string can't be determined
     * @return boolean equivalent
     * @throws OpenStegoException
     */
    private boolean determineBooleanEquivalent(String value, int errorCode) throws OpenStegoException {
        if (value.equalsIgnoreCase(TRUE_VALUE) || value.equalsIgnoreCase("y") || value.equals("1")) {
            return true;
        } else if (value.equalsIgnoreCase(FALSE_VALUE) || value.equalsIgnoreCase("n") || value.equals("0")) {
            return false;
        } else {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, errorCode, value);
        }
    }

    /**
     * Get method for configuration item - useCompression
     *
     * @return useCompression
     */
    public boolean isUseCompression() {
        return this.useCompression;
    }

    /**
     * Set method for configuration item - useCompression
     *
     * @param useCompression
     */
    public void setUseCompression(boolean useCompression) {
        this.useCompression = useCompression;
    }

    /**
     * Get Method for useEncryption
     *
     * @return useEncryption
     */
    public boolean isUseEncryption() {
        return this.useEncryption;
    }

    /**
     * Set Method for useEncryption
     *
     * @param useEncryption
     */
    public void setUseEncryption(boolean useEncryption) {
        this.useEncryption = useEncryption;
    }

    /**
     * Get Method for password
     *
     * @return password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set Method for password
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get Method for encryptionAlgorithm
     *
     * @return encryptionAlgorithm
     */
    public String getEncryptionAlgorithm() {
        return this.encryptionAlgorithm;
    }

    /**
     * Set Method for encryptionAlgorithm
     *
     * @param encryptionAlgorithm
     */
    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }
}
