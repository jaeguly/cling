package org.oflab.cling.mediaserver.android.content;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.ImageItem;
import org.seamless.util.MimeType;

public class AllImageContainer extends MediaStoreContainer {

    public AllImageContainer(String id, String parentId, String title) {
        super(id, parentId, title);
    }

    @Override
    public int update(Context context, String baseUrl) {
        final String where = null;
        final String[] whereVal = null;
        final String orderBy = null;

        Cursor cursor = context.getContentResolver().query(uri, columns, where, whereVal, orderBy);
        int childCount = 0;

        if (cursor.moveToFirst()) {
            do {
                String id = "I" + cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                long fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                long height = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT));
                long width = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH));

                String duration = null;
                long bitrate = 0;
                String value = baseUrl + "/" + id + filePath;

//                Res res = new Res(mimeType, fileSize, duration, bitrate, value);
//                Res res = new Res(new ProtocolInfo(mimeType), fileSize, value);
                int slash = mimeType.indexOf('/');
                Res res = new Res(
                        new MimeType(mimeType.substring(0, slash), mimeType.substring(slash + 1)),
                        fileSize, value);
                res.setResolution((int)width, (int)height);

                addItem(new ImageItem(id, parentID, title, null, res));
                ++childCount;

            } while (cursor.moveToNext());
        }

        cursor.close();

        setChildCount(childCount);
        return childCount;
    }

    protected static final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    protected static final String[] columns = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.WIDTH,
    };
}
