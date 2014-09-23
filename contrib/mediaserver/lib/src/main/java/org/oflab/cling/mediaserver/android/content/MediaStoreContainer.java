package org.oflab.cling.mediaserver.android.content;

import android.content.Context;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;

public abstract class MediaStoreContainer extends BasicContainer {
    public MediaStoreContainer(String id, String parentId, String title) {
        super(id, parentId, title);
    }

    /**
     *
     * @param context
     * @return child count
     *  if returns -1, fail
     */
    public abstract int update(Context context, String baseUrl);
}
