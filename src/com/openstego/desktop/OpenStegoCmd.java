/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2017 Samir Vaidya
 */

package com.openstego.desktop;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.openstego.desktop.util.CommonUtil;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.PluginManager;
import com.openstego.desktop.util.cmd.CmdLineOption;
import com.openstego.desktop.util.cmd.CmdLineOptions;
import com.openstego.desktop.util.cmd.CmdLineParser;
import com.openstego.desktop.util.cmd.PasswordInput;

/**
 * This is the main class for OpenStego command line
 */
public class OpenStegoCmd {
    /**
     * Private constructor to prevent instantiation
     */
    private OpenStegoCmd() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    /**
     * Command option for embed
     */
    public static final String COMMAND_EMBED = "embed";

    /**
     * Command option for extract
     */
    public static final String COMMAND_EXTRACT = "extract";

    /**
     * Command option for gensig
     */
    public static final String COMMAND_GENSIG = "gensig";

    /**
     * Command option for embedmark
     */
    public static final String COMMAND_EMBEDMARK = "embedmark";

    /**
     * Command option for checkmark
     */
    public static final String COMMAND_CHECKMARK = "checkmark";

    /**
     * Command option for diff
     */
    public static final String COMMAND_DIFF = "diff";

    /**
     * Command option for help
     */
    public static final String COMMAND_HELP = "help";

    /**
     * Command option for algorithms
     */
    public static final String COMMAND_ALGORITHMS = "algorithms";

    /**
     * Command option for readformats
     */
    public static final String COMMAND_READFORMATS = "readformats";

    /**
     * Command option for writeformats
     */
    public static final String COMMAND_WRITEFORMATS = "writeformats";

    private static final String LABEL_ENTERPW = "cmd.msg.enterPassword";
    private static final String LOGGER_NAME = "com.openstego.desktop";

    /**
     * Main method for processing command line
     *
     * @param args Command line arguments
     */
    public static void execute(String[] args) {
        String msgFileName = null;
        String sigFileName = null;
        String coverFileName = null;
        String stegoFileName = null;
        String extractDir = null;
        String extractFileName = null;
        String signatureFileName = null;
        String command = null;
        String pluginName = null;
        List<?> msgData = null;
        List<File> coverFileList = null;
        List<File> stegoFileList = null;
        OpenStego stego = null;
        CmdLineParser parser = null;
        CmdLineOptions options = null;
        CmdLineOption option = null;
        List<CmdLineOption> optionList = null;
        OpenStegoPlugin plugin = null;

        try {
            // First parse of the command-line (without plugin specific options)
            parser = new CmdLineParser(getStdCmdLineOptions(null), args);
            if (!parser.isValid()) {
                displayUsage();
                return;
            }

            pluginName = parser.getParsedOptions().getOptionValue("-a");

            // Get the plugin object
            if (pluginName != null && !pluginName.equals("")) {
                plugin = PluginManager.getPluginByName(pluginName);
                if (plugin == null) {
                    throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_NOT_FOUND, pluginName);
                }
            }
            // Try to auto-select plugin
            else {
                List<OpenStegoPlugin> plugins = PluginManager.getPlugins();
                if (plugins.size() == 1) {
                    plugin = plugins.get(0);
                } else if (plugins.size() > 1) {
                    optionList = parser.getParsedOptionsAsList();
                    if (!optionList.isEmpty()) {
                        command = (optionList.get(0)).getName();
                        if (command.equals(COMMAND_EMBED) || command.equals(COMMAND_EXTRACT)) {
                            plugins = PluginManager.getDataHidingPlugins();
                            if (plugins.size() == 1) {
                                plugin = plugins.get(0);
                            }
                        } else if (command.equals(COMMAND_GENSIG) || command.equals(COMMAND_EMBEDMARK) || command.equals(COMMAND_CHECKMARK)) {
                            plugins = PluginManager.getWatermarkingPlugins();
                            if (plugins.size() == 1) {
                                plugin = plugins.get(0);
                            }
                        }
                    }
                }
            }

            // Second parse of the command-line (with plugin specific options)
            if (plugin != null) {
                parser = new CmdLineParser(getStdCmdLineOptions(plugin), args);
            }

            optionList = parser.getParsedOptionsAsList();
            options = parser.getParsedOptions();

            for (int i = 0; i < optionList.size(); i++) {
                option = optionList.get(i);
                if (((i == 0) && (option.getType() != CmdLineOption.TYPE_COMMAND)) || ((i > 0) && (option.getType() == CmdLineOption.TYPE_COMMAND))) {
                    displayUsage();
                    return;
                }

                if (i == 0) {
                    command = option.getName();
                }
            }

            // Non-standard options are not allowed
            if (!parser.getNonStdOptions().isEmpty()) {
                displayUsage();
                return;
            }

            // Check that algorithm is selected
            if (!command.equals(COMMAND_HELP) && !command.equals(COMMAND_ALGORITHMS)) {
                if (plugin == null) {
                    throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.NO_PLUGIN_SPECIFIED);
                } else {
                    // Create main stego object
                    stego = new OpenStego(plugin, plugin.createConfig(parser.getParsedOptions()));
                }
            }

            if (command.equals(COMMAND_EMBED)) {
                msgFileName = options.getOptionValue("-mf");
                coverFileName = options.getOptionValue("-cf");
                stegoFileName = options.getOptionValue("-sf");

                // Check if we need to prompt for password
                if (stego.getConfig().isUseEncryption() && stego.getConfig().getPassword() == null) {
                    stego.getConfig().setPassword(PasswordInput.readPassword(labelUtil.getString(LABEL_ENTERPW) + " "));
                }

                coverFileList = CommonUtil.parseFileList(coverFileName, ";");
                // If no coverfile or only one coverfile is provided then use stegofile name given by the user
                if (coverFileList.size() <= 1) {
                    if (coverFileList.isEmpty() && coverFileName != null && !coverFileName.equals("-")) {
                        Logger.getLogger(LOGGER_NAME).log(Level.INFO, labelUtil.getString("cmd.msg.coverFileNotFound", coverFileName));
                        return;
                    }

                    CommonUtil.writeFile(
                        stego.embedData((msgFileName == null || msgFileName.equals("-")) ? null : new File(msgFileName),
                            coverFileList.isEmpty() ? null : coverFileList.get(0),
                            (stegoFileName == null || stegoFileName.equals("-")) ? null : stegoFileName),
                        (stegoFileName == null || stegoFileName.equals("-")) ? null : stegoFileName);
                }
                // Else loop through all coverfiles and overwrite the same coverfiles with generated stegofiles
                else {
                    // If stego file name is provided, then warn user that it will be ignored
                    if (stegoFileName != null && !stegoFileName.equals("-")) {
                        Logger.getLogger(LOGGER_NAME).log(Level.WARNING, labelUtil.getString("cmd.warn.stegoFileIgnored"));
                    }

                    // Loop through all cover files
                    for (int i = 0; i < coverFileList.size(); i++) {
                        coverFileName = (coverFileList.get(i)).getName();
                        CommonUtil.writeFile(stego.embedData((msgFileName == null || msgFileName.equals("-")) ? null : new File(msgFileName),
                            coverFileList.get(i), coverFileName), coverFileName);

                        Logger.getLogger(LOGGER_NAME).log(Level.INFO, labelUtil.getString("cmd.msg.coverProcessed", coverFileName));
                    }
                }
            } else if (command.equals(COMMAND_EMBEDMARK)) {
                sigFileName = options.getOptionValue("-gf");
                coverFileName = options.getOptionValue("-cf");
                stegoFileName = options.getOptionValue("-sf");

                coverFileList = CommonUtil.parseFileList(coverFileName, ";");
                // If no coverfile or only one coverfile is provided then use stegofile name given by the user
                if (coverFileList.size() <= 1) {
                    if (coverFileList.isEmpty() && coverFileName != null && !coverFileName.equals("-")) {
                        Logger.getLogger(LOGGER_NAME).log(Level.INFO, labelUtil.getString("cmd.msg.coverFileNotFound", coverFileName));
                        return;
                    }

                    CommonUtil.writeFile(
                        stego.embedMark((sigFileName == null || sigFileName.equals("-")) ? null : new File(sigFileName),
                            coverFileList.isEmpty() ? null : coverFileList.get(0),
                            (stegoFileName == null || stegoFileName.equals("-")) ? null : stegoFileName),
                        (stegoFileName == null || stegoFileName.equals("-")) ? null : stegoFileName);
                }
                // Else loop through all coverfiles and overwrite the same coverfiles with generated stegofiles
                else {
                    // If stego file name is provided, then warn user that it will be ignored
                    if (stegoFileName != null && !stegoFileName.equals("-")) {
                        Logger.getLogger(LOGGER_NAME).log(Level.WARNING, labelUtil.getString("cmd.warn.stegoFileIgnored"));
                    }

                    // Loop through all cover files
                    for (int i = 0; i < coverFileList.size(); i++) {
                        coverFileName = (coverFileList.get(i)).getName();
                        CommonUtil.writeFile(stego.embedMark((sigFileName == null || sigFileName.equals("-")) ? null : new File(sigFileName),
                            coverFileList.get(i), coverFileName), coverFileName);

                        Logger.getLogger(LOGGER_NAME).log(Level.INFO, labelUtil.getString("cmd.msg.coverProcessed", coverFileName));
                    }
                }
            } else if (command.equals(COMMAND_EXTRACT)) {
                stegoFileName = options.getOptionValue("-sf");
                extractDir = options.getOptionValue("-xd");

                if (stegoFileName == null) {
                    displayUsage();
                    return;
                }

                try {
                    msgData = stego.extractData(new File(stegoFileName));
                } catch (OpenStegoException osEx) {
                    if (osEx.getErrorCode() == OpenStegoException.INVALID_PASSWORD || osEx.getErrorCode() == OpenStegoException.NO_VALID_PLUGIN) {
                        if (stego.getConfig().getPassword() == null) {
                            stego.getConfig().setPassword(PasswordInput.readPassword(labelUtil.getString(LABEL_ENTERPW) + " "));

                            try {
                                msgData = stego.extractData(new File(stegoFileName));
                            } catch (OpenStegoException inEx) {
                                if (inEx.getErrorCode() == OpenStegoException.INVALID_PASSWORD) {
                                    Logger.getLogger(LOGGER_NAME).log(Level.SEVERE, inEx.getMessage(), inEx);
                                    return;
                                } else {
                                    throw inEx;
                                }
                            }
                        } else {
                            Logger.getLogger(LOGGER_NAME).log(Level.SEVERE, osEx.getMessage(), osEx);
                            return;
                        }
                    } else {
                        throw osEx;
                    }
                }
                extractFileName = options.getOptionValue("-xf");
                if (extractFileName == null) {
                    extractFileName = (String) msgData.get(0);
                    if (extractFileName == null || extractFileName.equals("")) {
                        extractFileName = "untitled";
                    }
                }
                if (extractDir != null) {
                    extractFileName = extractDir + File.separator + extractFileName;
                }

                CommonUtil.writeFile((byte[]) msgData.get(1), extractFileName);
                Logger.getLogger(LOGGER_NAME).log(Level.INFO, labelUtil.getString("cmd.msg.fileExtracted", extractFileName));
            } else if (command.equals(COMMAND_CHECKMARK)) {
                stegoFileName = options.getOptionValue("-sf");
                sigFileName = options.getOptionValue("-gf");

                if (stegoFileName == null || sigFileName == null) {
                    displayUsage();
                    return;
                }

                stegoFileList = CommonUtil.parseFileList(stegoFileName, ";");
                // If only one stegofile is provided then use stegofile name given by the user
                if (stegoFileList.size() == 1) {
                    Logger.getLogger(LOGGER_NAME).log(Level.INFO, Double.toString(stego.checkMark(stegoFileList.get(0), new File(sigFileName))));
                }
                // Else loop through all stegofiles and calculate correlation value for each
                else {
                    for (int i = 0; i < stegoFileList.size(); i++) {
                        stegoFileName = (stegoFileList.get(i)).getName();
                        Logger.getLogger(LOGGER_NAME).log(Level.INFO,
                            stegoFileName + "\t" + stego.checkMark(stegoFileList.get(i), new File(sigFileName)));
                    }
                }
            } else if (command.equals(COMMAND_GENSIG)) {
                // Check if we need to prompt for password
                if (stego.getConfig().getPassword() == null) {
                    stego.getConfig().setPassword(PasswordInput.readPassword(labelUtil.getString(LABEL_ENTERPW) + " "));
                }

                signatureFileName = options.getOptionValue("-gf");
                CommonUtil.writeFile(stego.generateSignature(),
                    (signatureFileName == null || signatureFileName.equals("-")) ? null : signatureFileName);
            } else if (command.equals(COMMAND_DIFF)) {
                coverFileName = options.getOptionValue("-cf");
                stegoFileName = options.getOptionValue("-sf");
                extractDir = options.getOptionValue("-xd");
                extractFileName = options.getOptionValue("-xf");

                if (extractDir != null) {
                    extractFileName = extractDir + File.separator + extractFileName;
                }

                CommonUtil.writeFile(stego.getDiff(new File(stegoFileName), new File(coverFileName), extractFileName), extractFileName);
            } else if (command.equals(COMMAND_READFORMATS)) {
                List<String> formats = plugin.getReadableFileExtensions();
                for (int i = 0; i < formats.size(); i++) {
                    Logger.getLogger(LOGGER_NAME).log(Level.INFO, formats.get(i));
                }
            } else if (command.equals(COMMAND_WRITEFORMATS)) {
                List<String> formats = plugin.getWritableFileExtensions();
                for (int i = 0; i < formats.size(); i++) {
                    Logger.getLogger(LOGGER_NAME).log(Level.INFO, formats.get(i));
                }
            } else if (command.equals(COMMAND_ALGORITHMS)) {
                List<OpenStegoPlugin> plugins = PluginManager.getPlugins();
                for (int i = 0; i < plugins.size(); i++) {
                    plugin = plugins.get(i);
                    Logger.getLogger(LOGGER_NAME).log(Level.INFO,
                        plugin.getName() + " " + plugin.getPurposesLabel() + " - " + plugin.getDescription());
                }
            } else if (command.equals(COMMAND_HELP)) {
                if (plugin == null) {
                    displayUsage();
                } else
                // Show plugin-specific help
                {
                    Logger.getLogger(LOGGER_NAME).log(Level.INFO, plugin.getUsage());
                }
            } else {
                displayUsage();
            }
        } catch (OpenStegoException osEx) {
            Logger.getLogger(LOGGER_NAME).log(Level.SEVERE, osEx.getMessage(), osEx);
        } catch (Exception ex) {
            Logger.getLogger(LOGGER_NAME).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * Method to display usage for OpenStego
     *
     * @throws OpenStegoException
     */
    private static void displayUsage() throws OpenStegoException {
        PluginManager.loadPlugins();

        System.err.print(labelUtil.getString("appName") + " " + labelUtil.getString("appVersion") + ". ");
        System.err.println(labelUtil.getString("copyright") + "\n");
        System.err.println(labelUtil.getString("cmd.usage", File.separator));
    }

    /**
     * Method to generate the standard list of command-line options
     *
     * @param plugin Stego plugin for plugin-specific command-line options
     * @return Standard list of command-line options
     * @throws OpenStegoException
     */
    private static CmdLineOptions getStdCmdLineOptions(OpenStegoPlugin plugin) throws OpenStegoException {
        CmdLineOptions options = new CmdLineOptions();

        // Commands
        options.add(COMMAND_EMBED, "--embed", CmdLineOption.TYPE_COMMAND, false);
        options.add(COMMAND_EXTRACT, "--extract", CmdLineOption.TYPE_COMMAND, false);
        options.add(COMMAND_GENSIG, "--gensig", CmdLineOption.TYPE_COMMAND, false);
        options.add(COMMAND_EMBEDMARK, "--embedmark", CmdLineOption.TYPE_COMMAND, false);
        options.add(COMMAND_CHECKMARK, "--checkmark", CmdLineOption.TYPE_COMMAND, false);
        options.add(COMMAND_DIFF, "--diff", CmdLineOption.TYPE_COMMAND, false);
        options.add(COMMAND_READFORMATS, "--readformats", CmdLineOption.TYPE_COMMAND, false);
        options.add(COMMAND_WRITEFORMATS, "--writeformats", CmdLineOption.TYPE_COMMAND, false);
        options.add(COMMAND_ALGORITHMS, "--algorithms", CmdLineOption.TYPE_COMMAND, false);
        options.add(COMMAND_HELP, "--help", CmdLineOption.TYPE_COMMAND, false);

        // Plugin options
        options.add("-a", "--algorithm", CmdLineOption.TYPE_OPTION, true);

        // File options
        options.add("-mf", "--messagefile", CmdLineOption.TYPE_OPTION, true);
        options.add("-cf", "--coverfile", CmdLineOption.TYPE_OPTION, true);
        options.add("-sf", "--stegofile", CmdLineOption.TYPE_OPTION, true);
        options.add("-xf", "--extractfile", CmdLineOption.TYPE_OPTION, true);
        options.add("-xd", "--extractdir", CmdLineOption.TYPE_OPTION, true);
        options.add("-gf", "--sigfile", CmdLineOption.TYPE_OPTION, true);

        // Command options
        options.add("-c", "--compress", CmdLineOption.TYPE_OPTION, false);
        options.add("-C", "--nocompress", CmdLineOption.TYPE_OPTION, false);
        options.add("-e", "--encrypt", CmdLineOption.TYPE_OPTION, false);
        options.add("-E", "--noencrypt", CmdLineOption.TYPE_OPTION, false);
        options.add("-p", "--password", CmdLineOption.TYPE_OPTION, true);
        options.add("-A", "--cryptalgo", CmdLineOption.TYPE_OPTION, true);

        // Plugin-specific options
        if (plugin != null) {
            plugin.populateStdCmdLineOptions(options);
        }

        return options;
    }
}
