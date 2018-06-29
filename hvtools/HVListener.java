/**
 * HVListener - interface to update messages of Status window in HVmainMenu window
 * @version 1.1
 * Last update: 17-May01
 */

package hvtools;

public interface HVListener {
    /**
     * update (append) new string with message in Status window
     * @param message String message to append.
     */
    public void updateStatus(String message);

}
