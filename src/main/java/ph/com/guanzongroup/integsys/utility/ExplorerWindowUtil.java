package ph.com.guanzongroup.integsys.utility;

import org.guanzon.appdriver.agent.ShowMessageFX;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;


/**
 * Utility for opening Windows File Explorer at a given file's location,
 * reusing an already-open Explorer window for the same folder instead of
 * spawning a new one.
 * <p>
 * This class is intended to be shared across multiple forms/controllers in
 * the application. Each calling form is responsible for resolving its own
 * attachment file name and full file path (since that logic is typically
 * form-specific, e.g. looking up a temp image cache or a controller model),
 * and then delegates validation, user confirmation, and the actual
 * Explorer-open/reuse behavior to this class.
 * <p>
 * Internally, this utility shells out to Windows PowerShell to interact with
 * the {@code Shell.Application} COM object. This allows it to:
 * <ol>
 *   <li>Enumerate all currently open Explorer windows.</li>
 *   <li>Compare each window's current folder path (normalized) against the
 *       target folder derived from the requested file.</li>
 *   <li>If a matching window is found, bring it to the foreground and
 *       reselect the requested file within it, instead of opening a
 *       duplicate Explorer window.</li>
 *   <li>If no matching window is found, fall back to launching a new
 *       Explorer window with the file selected, via
 *       {@code explorer.exe /select,"path"}.</li>
 * </ol>
 * <p>
 * This class is a stateless, static utility and cannot be instantiated.
 * Each call is self-contained: it spawns its own PowerShell process and
 * performs a fresh scan of open Explorer windows, so there is no shared or
 * cached state between invocations. Callers that may invoke this rapidly
 * (e.g. via double-click or a hotkey) should consider debouncing at the
 * call site, since each call starts a new process.
 * <p>
 * <b>Platform requirement:</b> this utility is Windows-only. It relies on
 * {@code powershell.exe}, the {@code Shell.Application} COM object, and
 * {@code user32.dll} (via an inline C# type compiled at runtime through
 * {@code Add-Type}). It will not function correctly on non-Windows
 * platforms.
 *
 * @author TEEJEI DE CELIS
 * @version 1.0
 * @since 2026-09-16
 */
public final class ExplorerWindowUtil {

    /**
     * Private constructor to prevent instantiation. This class exposes only
     * static utility methods and holds no instance state.
     */
    private ExplorerWindowUtil() {
        // static utility class, no instances
    }

    /**
     * Validates a resolved attachment file path, asks the user to confirm,
     * and opens/focuses Windows Explorer at that file if the user confirms.
     * <p>
     * This is the primary entry point most calling forms should use. It
     * performs the following steps in order:
     * <ol>
     *   <li>Warns and returns if {@code lsFileName} is {@code null} or empty.</li>
     *   <li>Warns and returns if {@code lsFilePath} is {@code null} or empty.</li>
     *   <li>Warns and returns if no file exists at {@code lsFilePath}.</li>
     *   <li>Prompts the user with an Okay/Cancel confirmation dialog.</li>
     *   <li>If confirmed, delegates to
     *       {@link #openOrFocusExplorerAndSelect(Path)} to open or reuse an
     *       Explorer window and select the file.</li>
     * </ol>
     * All warning dialogs are shown via {@link ShowMessageFX}, matching the
     * messaging previously embedded directly in calling forms.
     *
     * @param lsFileName the display name of the attachment, used in
     *                    confirmation/warning messages shown to the user;
     *                    if {@code null} or empty, a warning is shown and
     *                    the method returns without opening Explorer
     * @param lsFilePath  the fully resolved path to the attachment file on
     *                    disk; if {@code null}, empty, or does not point to
     *                    an existing file, a warning is shown and the
     *                    method returns without opening Explorer
     * @throws IOException if the underlying PowerShell process used to
     *                      open or focus Explorer could not be started
     */
    public static void confirmAndOpenAttachmentLocation(String lsFileName, String lsFilePath) throws IOException {
        if (lsFileName == null || lsFileName.isEmpty()) {
            ShowMessageFX.Warning(
                    "Attachment file not found.",
                    "Attachment",
                    null
            );
            return;
        }

        if (lsFilePath == null || lsFilePath.isEmpty()) {
            ShowMessageFX.Warning(
                    "Attachment file location was not found.",
                    "Attachment",
                    null
            );
            return;
        }

        Path loPath = Paths.get(lsFilePath);

        if (!Files.exists(loPath)) {
            ShowMessageFX.Warning(
                    "The attachment file does not exist.\n\n" + lsFilePath,
                    "Attachment",
                    null
            );
            return;
        }

        String lsMessage = "Do you want to open the file location of this attachment?\n\n"
                + lsFileName;

        if (ShowMessageFX.OkayCancel(
                lsMessage,
                "Open Attachment Location",
                null)) {

            openOrFocusExplorerAndSelect(loPath);
        }
    }

    /**
     * Opens Windows Explorer with the given file selected, given the file
     * path as a {@link String}.
     * <p>
     * If an Explorer window is already open for the file's parent folder,
     * that existing window is brought to the foreground and the file is
     * reselected in it, rather than opening a new Explorer window. If no
     * matching window is open, a new Explorer window is launched with the
     * file selected.
     * <p>
     * This overload simply converts {@code psFilePath} to a {@link Path}
     * and delegates to {@link #openOrFocusExplorerAndSelect(Path)}. Use
     * this when the caller only has a path as a raw string.
     *
     * @param psFilePath the absolute or relative path to the file to
     *                    select in Explorer; if {@code null} or empty,
     *                    this method returns immediately and does nothing
     * @throws IOException if the underlying PowerShell process could not
     *                      be started
     */
    public static void openOrFocusExplorerAndSelect(String psFilePath) throws IOException {
        if (psFilePath == null || psFilePath.isEmpty()) {
            return;
        }
        openOrFocusExplorerAndSelect(Paths.get(psFilePath));
    }

    /**
     * Opens Windows Explorer with the given file selected, given the file
     * path as a {@link Path}.
     * <p>
     * The supplied path is first resolved to an absolute, normalized form
     * so that equivalent paths (e.g. differing in trailing separators or
     * slash direction) are treated consistently. A PowerShell script is
     * then generated and executed to:
     * <ul>
     *   <li>Search currently open Explorer windows for one whose folder
     *       matches the target folder;</li>
     *   <li>Reuse and refocus that window if found, reselecting the target
     *       file within it;</li>
     *   <li>Otherwise, launch a new Explorer window with the file
     *       selected.</li>
     * </ul>
     * The PowerShell process is started asynchronously (fire-and-forget);
     * this method does not block waiting for the script to finish, and
     * does not capture or return its output.
     *
     * @param poPath the path to the file to select in Explorer; if
     *               {@code null}, this method returns immediately and
     *               does nothing
     * @throws IOException if the underlying PowerShell process could not
     *                      be started
     */
    public static void openOrFocusExplorerAndSelect(Path poPath) throws IOException {
        if (poPath == null) {
            return;
        }

        Path loAbsolute = poPath.toAbsolutePath().normalize();
        Path loFolder = loAbsolute.getParent();
        String lsFileName = loAbsolute.getFileName().toString();
        String lsFolderPath = (loFolder != null) ? loFolder.toString() : loAbsolute.toString();
        String lsFullPath = loAbsolute.toString();

        String lsScript = buildExplorerReusePsScript(lsFolderPath, lsFileName, lsFullPath);

        String lsEncoded = Base64.getEncoder().encodeToString(
                lsScript.getBytes(StandardCharsets.UTF_16LE)
        );

        ProcessBuilder loBuilder = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-NonInteractive",
                "-WindowStyle", "Hidden",
                "-ExecutionPolicy", "Bypass",
                "-EncodedCommand", lsEncoded
        );

        loBuilder.redirectErrorStream(true);
        loBuilder.start();
    }

    /**
     * Builds the PowerShell script used to reuse or open an Explorer window
     * for a target file.
     * <p>
     * The generated script performs the following, in order:
     * <ol>
     *   <li>Defines a {@code Normalize-Path} helper that trims trailing
     *       {@code \} / {@code /} separators and lower-cases the path, so
     *       that folder paths can be compared reliably regardless of
     *       trailing-slash or case differences.</li>
     *   <li>Creates a {@code Shell.Application} COM object and enumerates
     *       all of its open windows, filtering to those belonging to
     *       {@code explorer.exe}.</li>
     *   <li>Compares each open Explorer window's current folder path
     *       (normalized) against the normalized target folder path.</li>
     *   <li>If a match is found: compiles a small inline C# helper type via
     *       {@code Add-Type} that wraps the {@code user32.dll} functions
     *       {@code SetForegroundWindow}, {@code ShowWindow}, and
     *       {@code IsIconic}; uses it to restore the window if minimized
     *       and bring it to the foreground; then calls
     *       {@code SelectItem} on the window's shell folder document to
     *       reselect the target file.</li>
     *   <li>If no match is found: falls back to
     *       {@code Start-Process explorer.exe -ArgumentList "/select,..."}
     *       to open a new Explorer window with the file selected.</li>
     * </ol>
     * All string values are escaped via {@link #psEscape(String)} before
     * being embedded in the script to guard against breaking out of the
     * surrounding double-quoted PowerShell string literals.
     *
     * @param lsFolderPath the full path to the folder containing the
     *                      target file; used to find a matching, already
     *                      open Explorer window
     * @param lsFileName    the file name (without directory) of the target
     *                      file, used to locate and reselect the item
     *                      within the matched Explorer window via
     *                      {@code ParseName}
     * @param lsFullPath    the full path (folder plus file name) of the
     *                      target file, used as the argument to
     *                      {@code explorer.exe /select,} when no existing
     *                      window is found
     * @return the complete PowerShell script, as a single string, ready to
     *         be Base64/UTF-16LE encoded and passed to
     *         {@code powershell.exe -EncodedCommand}
     */
    private static String buildExplorerReusePsScript(String lsFolderPath, String lsFileName, String lsFullPath) {
        String lsFolderEsc = psEscape(lsFolderPath);
        String lsFileEsc = psEscape(lsFileName);
        String lsFullEsc = psEscape(lsFullPath);

        StringBuilder loSb = new StringBuilder();

        loSb.append("$ErrorActionPreference = 'SilentlyContinue'\n");
        loSb.append("function Normalize-Path($p) {\n");
        loSb.append("    if ([string]::IsNullOrEmpty($p)) { return '' }\n");
        loSb.append("    $p = $p.TrimEnd('\\','/')\n");
        loSb.append("    return $p.ToLowerInvariant()\n");
        loSb.append("}\n\n");

        loSb.append("$targetFolder = \"").append(lsFolderEsc).append("\"\n");
        loSb.append("$targetFile = \"").append(lsFileEsc).append("\"\n");
        loSb.append("$targetFull = \"").append(lsFullEsc).append("\"\n");
        loSb.append("$normTarget = Normalize-Path $targetFolder\n\n");

        loSb.append("$shell = New-Object -ComObject Shell.Application\n");
        loSb.append("$foundWindow = $null\n\n");

        loSb.append("foreach ($win in $shell.Windows()) {\n");
        loSb.append("    try {\n");
        loSb.append("        if ($win.FullName -and $win.FullName.ToLowerInvariant().EndsWith('explorer.exe')) {\n");
        loSb.append("            if ($win.Document -and $win.Document.Folder -and $win.Document.Folder.Self) {\n");
        loSb.append("                $winPath = $win.Document.Folder.Self.Path\n");
        loSb.append("                $normWin = Normalize-Path $winPath\n");
        loSb.append("                if ($normWin -eq $normTarget) {\n");
        loSb.append("                    $foundWindow = $win\n");
        loSb.append("                    break\n");
        loSb.append("                }\n");
        loSb.append("            }\n");
        loSb.append("        }\n");
        loSb.append("    } catch { }\n");
        loSb.append("}\n\n");

        loSb.append("if ($foundWindow -ne $null) {\n");
        loSb.append("    Add-Type @\"\n");
        loSb.append("using System;\n");
        loSb.append("using System.Runtime.InteropServices;\n");
        loSb.append("public class ExplorerFocusHelper {\n");
        loSb.append("    [DllImport(\"user32.dll\")]\n");
        loSb.append("    public static extern bool SetForegroundWindow(IntPtr hWnd);\n");
        loSb.append("    [DllImport(\"user32.dll\")]\n");
        loSb.append("    public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);\n");
        loSb.append("    [DllImport(\"user32.dll\")]\n");
        loSb.append("    public static extern bool IsIconic(IntPtr hWnd);\n");
        loSb.append("}\n");
        loSb.append("\"@\n\n");

        loSb.append("    try {\n");
        loSb.append("        $hwnd = [IntPtr]$foundWindow.HWND\n");
        loSb.append("        if ([ExplorerFocusHelper]::IsIconic($hwnd)) {\n");
        loSb.append("            [ExplorerFocusHelper]::ShowWindow($hwnd, 9) | Out-Null\n");
        loSb.append("        }\n");
        loSb.append("        [ExplorerFocusHelper]::SetForegroundWindow($hwnd) | Out-Null\n");
        loSb.append("    } catch { }\n\n");

        loSb.append("    try {\n");
        loSb.append("        $item = $foundWindow.Document.Folder.ParseName($targetFile)\n");
        loSb.append("        if ($item -ne $null) {\n");
        loSb.append("            # 1=DESELECTOTHERS, 4=ENSUREVISIBLE, 8=FOCUSED, 16=SELECT\n");
        loSb.append("            $foundWindow.Document.SelectItem($item, 29)\n");
        loSb.append("        }\n");
        loSb.append("    } catch { }\n");
        loSb.append("} else {\n");
        loSb.append("    Start-Process explorer.exe -ArgumentList \"/select,`\"$targetFull`\"\"\n");
        loSb.append("}\n");

        return loSb.toString();
    }

    /**
     * Escapes a value for safe embedding inside a double-quoted PowerShell
     * string literal.
     * <p>
     * Escapes the PowerShell backtick escape character itself, double
     * quotes, and the dollar sign (to prevent unintended variable
     * expansion or subexpression evaluation), so that arbitrary file and
     * folder names can be safely interpolated into the generated script.
     *
     * @param lsValue the raw value to escape; may be {@code null}
     * @return the escaped value, safe to place inside a double-quoted
     *         PowerShell string; returns an empty string if
     *         {@code lsValue} is {@code null}
     */
    private static String psEscape(String lsValue) {
        if (lsValue == null) {
            return "";
        }
        return lsValue
                .replace("`", "``")
                .replace("\"", "`\"")
                .replace("$", "`$");
    }
}