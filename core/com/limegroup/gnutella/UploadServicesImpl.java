package com.limegroup.gnutella;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.settings.UploadSettings;
import com.limegroup.gnutella.uploader.UploadSlotManager;

@Singleton
public class UploadServicesImpl implements UploadServices {
    
    public static boolean IS_SEEDING_HOSTILES_TXT = false;
    
    private final Provider<UploadSlotManager> uploadSlotManager;
    private final Provider<ConnectionManager> connectionManager;
    
    @Inject
    public UploadServicesImpl(
            Provider<UploadSlotManager> uploadSlotManager,
            Provider<ConnectionManager> connectionManager) {
        this.uploadSlotManager = uploadSlotManager;
        this.connectionManager = connectionManager;
    }
    

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.UploadServices#hasActiveUploads()
     */
    public boolean hasActiveUploads() {
        uploadSlotManager.get().measureBandwidth();
        try {
            return uploadSlotManager.get().getMeasuredBandwidth() > 0;
        } catch (InsufficientDataException ide) {
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.UploadServices#getRequestedUploadSpeed()
     */
    public float getRequestedUploadSpeed() {
        // if the user chose not to limit his uploads
        // by setting the upload speed to unlimited
        // set the upload speed to 3.4E38 bytes per second.
        // This is de facto not limiting the uploads
        int uSpeed = UploadSettings.UPLOAD_SPEED.getValue();
        if (uSpeed == 100) {
            return Float.MAX_VALUE; 
        } else {
            // if the uploads are limited, take messageUpstream
            // for ultrapeers into account, - don't allow lower 
            // speeds than 1kb/s so uploads won't stall completely
            // if the user accidently sets his connection speed 
            // lower than his message upstream
    
            // connection speed is in kbits per second and upload speed is in percent
            float speed = ConnectionSettings.CONNECTION_SPEED.getValue() / 8f * uSpeed / 100f;
            
            // reduced upload speed if we are an ultrapeer
            speed -= connectionManager.get().getMeasuredUpstreamBandwidth();
            
            // we need bytes per second
            return Math.max(speed, 1f) * 1024f;
        }
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.UploadServices#getNumUploads()
     */
    public int getNumUploads() {
        return 0;//uploadManager.get().uploadsInProgress();
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.UploadServices#getNumQueuedUploads()
     */
    public int getNumQueuedUploads() {
        return 0;//uploadManager.get().getNumQueuedUploads();
    }
    
    public boolean isSeedingHostilesTxt() {
        return IS_SEEDING_HOSTILES_TXT;
    }


}
