package org.oflab.cling.mediaserver.android.content;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;

public class AllVideoContainer extends MediaStoreContainer {

    public AllVideoContainer(String id, String parentId, String title) {
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
                String id = "V" + cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                long fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                long height = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
                long width = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
                String creator = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));

                int slash = mimeType.indexOf('/');
                Res res = new Res(
                        new MimeType(mimeType.substring(0, slash), mimeType.substring(slash + 1)),
                        fileSize, baseUrl + "/" + id + filePath);
                res.setResolution((int)width, (int)height);
                res.setDuration(duration / (1000 * 60 * 60) + ":"
                        + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                        + (duration % (1000 * 60)) / 1000);

                addItem(new VideoItem("" + id, parentID, title, creator, res));
                ++childCount;

            } while (cursor.moveToNext());
        }

        cursor.close();

        setChildCount(childCount);
        return childCount;
    }

    protected static final Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    protected static final String[] columns = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.DURATION,
    };
}
