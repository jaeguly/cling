package org.fourthline.cling.mediaserver.android;

import android.util.Log;

import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.StorageFolder;

public class AndroidContentDirectoryService extends AbstractContentDirectoryService {


    @Override
    public BrowseResult browse(String objectID, BrowseFlag browseFlag, String filter,
                               long firstResult, long maxResults, SortCriterion[] orderBy)
            throws ContentDirectoryException {
        DIDLContent didlContent = new DIDLContent();

        int resultCount = 0;

        if (BrowseFlag.METADATA.equals(browseFlag)) {

            if ("0".equalsIgnoreCase(objectID)) {
                // build a root container
                StorageFolder rootContainer = new StorageFolder("0", "-1", "root", "me", 2, null);
                rootContainer.setClazz(new DIDLObject.Class("object.container"));
                rootContainer.setRestricted(true);

                didlContent.addContainer(rootContainer);
                resultCount = 1;
            }

        }

        if ("0".equalsIgnoreCase(objectID)) {

            // build a music container
            StorageFolder musicContainer = new StorageFolder("1", "0", "Music", null, 2, null);
            musicContainer.setClazz(new DIDLObject.Class("object.container"));
            musicContainer.setRestricted(true);

            // build a photo container
            StorageFolder photoContainer = new StorageFolder("2", "0", "Photo", null, 2, null);
            musicContainer.setClazz(new DIDLObject.Class("object.container"));
            musicContainer.setRestricted(true);

            didlContent.addContainer(musicContainer);
            didlContent.addContainer(photoContainer);
            resultCount = 2;
        }

        try {
            String didlString = new DIDLParser().generate(didlContent);
            return new BrowseResult(didlString, resultCount, resultCount);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public BrowseResult search(String containerId, String searchCriteria, String filter, long firstResult,
                               long maxResults, SortCriterion[] orderBy) throws ContentDirectoryException {
        // You can override this method to implement searching!

        return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
    }

    private static final String TAG = AndroidContentDirectoryService.class.getSimpleName();
}
