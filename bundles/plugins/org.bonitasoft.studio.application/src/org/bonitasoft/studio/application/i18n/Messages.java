/**
 * Copyright (C) 2009 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.application.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author Mickael Istria
 *
 */
public class Messages extends NLS {

    private static String BUNDLE_NAME = "messages"; //$NON-NLS-1$

    public static String openProcessWizardPage_title;
    public static String browseButton_label;
    public static String NewProcessButtonLabel;
    public static String OpenProcessButtonLabel;
    public static String SaveProcessButtonLabel;
    public static String PrintProcessButtonLabel;
    public static String ImportProcessButtonLabel;
    public static String CopyButtonLabel;
    public static String PasteButtonLabel;
    public static String HelpButtonLabel;
    public static String WelcomeButtonLabel;
    public static String importProcessTitle;
    public static String PreviewFormLabel;


    public static String importProcessProgressDialog;
    public static String unableTofindLogTitle;
    public static String unableTofindLogMessage;
    public static String confirmContributionInstall_message;
    public static String confirmContributionInstall_title;
    public static String error;
    public static String contributionSuccessfullyInstalled;
    public static String success;
    public static String notLoggedIn;
    public static String errorWhileDownloadingContrib;
    public static String importFileTitle;
    public static String importFileDescription;
    public static String selectImportLabel;
    public static String importDescriptionLabel;
    public static String selectFileToImport;
    public static String installingFromContribution;
    public static String downloading;
    public static String installingContribution;
    public static String badWorkspaceVersionTitle;
    public static String badWorkspaceVersionMessage;
    public static String PreferencesButtonLabel;
    public static String resfreshConnectors;
    public static String aboutText;
    public static String reduceCoolbarTooltip;
    public static String maximizeCoolbarTooltip;
    public static String errorWhileImporting_message;
    public static String errorWhileImporting_title;
    public static String initializingCurrentRepository;
    public static String shuttingDown;

    public static String duplicatingDiagram;

	public static String jreNotFoundTitle;

	public static String jreNotFoundMessage;

	public static String previewButton;
	public static String advancedPreviewButton;
	public static String lnfForPreview;
	public static String browserForPreview;
	public static String invalidWorkspaceTitle;
	public static String invalidWorkspace;
	public static String failedToOpenLogTitle;
	public static String failedToOpenLogMessage;

	public static String doNotDisplayForOtherDiagrams;




    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

}
