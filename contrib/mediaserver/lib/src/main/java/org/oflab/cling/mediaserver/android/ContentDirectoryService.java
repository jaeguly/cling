package org.oflab.cling.mediaserver.android;


import android.util.Log;

import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

public class ContentDirectoryService extends AbstractContentDirectoryService {

    ContentDirectoryService(MediaServer mediaServer) {
        this.mediaServer = mediaServer;
    }

    @Override
    public BrowseResult browse(String objectId, BrowseFlag browseFlag, String filter,
                               long firstResult, long maxResults, SortCriterion[] orderBy)
            throws ContentDirectoryException {

        Log.i(TAG, "browse(objectId:" + objectId + ", browseFlag:" + browseFlag
                + ", filter:" + filter + ", firstResult:" + firstResult
                + ", maxResults:" + maxResults + ", orderBy:" + orderBy);

        DIDLContent didlContent = new DIDLContent();
        int contentCount = 0;
        int matchCount = 0;

        DIDLObject object = mediaServer.findObjectById(objectId);

        if (object != null) {
            if (browseFlag.equals(BrowseFlag.METADATA)) {
                if (object instanceof Container) {
                    didlContent.addContainer((Container) object);
                    ++contentCount;
                    ++matchCount;
                } else if (object instanceof Item) {
                    didlContent.addItem((Item) object);
                    ++contentCount;
                    ++matchCount;
                } else {
                    Log.w(TAG, "undefined instance type!");
                }
            } else if (browseFlag.equals(BrowseFlag.DIRECT_CHILDREN)) {
                Container container = (Container) object;
                boolean maxReached = maxResults == 0;

                matchCount += container.getContainers().size();

                for (Container subContainer : container.getContainers()) {
                    if (maxReached)
                        break;

                    if (firstResult > 0 && contentCount == firstResult)
                        continue;

                    didlContent.addContainer(subContainer);
                    contentCount++;

                    if (contentCount >= maxResults)
                        maxReached = true;
                }

                matchCount += container.getItems().size();

                for (Item item : container.getItems()) {
                    if (maxReached)
                        break;

                    if (firstResult > 0 && contentCount == firstResult)
                        continue;

                    didlContent.addItem(item);
                    contentCount++;

                    if (contentCount >= maxResults)
                        maxReached = true;
                }
            } else {
                Log.w(TAG, "undefined browseFlag!");
            }
        } else {
            Log.w(TAG, "object not found!");
        }

        try {
            return new BrowseResult(new DIDLParser().generate(didlContent),
                    contentCount, matchCount);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final String TAG = ContentDirectoryService.class.getSimpleName();
    protected MediaServer mediaServer;
}
