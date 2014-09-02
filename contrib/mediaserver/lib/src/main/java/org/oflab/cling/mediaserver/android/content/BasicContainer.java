package org.oflab.cling.mediaserver.android.content;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;

public class BasicContainer extends Container {
    public static final String CREATOR = null;
    public static final DIDLObject.Class CLASS_CONTAINER = new DIDLObject.Class("object.container");

    public BasicContainer(String id, String parentId, String title) {
        super(id, parentId, title, CREATOR, CLASS_CONTAINER, 0);
    }

    public BasicContainer(String id, String parentId, String title, String creator) {
        super(id, parentId, title, creator, CLASS_CONTAINER, 0);
    }

    public Container addContainerAndCount(Container container) {
        this.addContainer(container);

        setChildCount(getChildCount() + 1);

        return container;
    }
}
