package org.oflab.cling.mediaserver.android.content;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;

public class AllAudioContainer extends MediaStoreContainer {

    public AllAudioContainer(String id, String parentId, String title) {
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
                String id = "A" + cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                long fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                String creator = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));

                int slash = mimeType.indexOf('/');
                Res res = new Res(
                        new MimeType(mimeType.substring(0, slash), mimeType.substring(slash + 1)),
                        fileSize, baseUrl + "/" + id + filePath);
                res.setDuration(duration / (1000 * 60 * 60) + ":"
                        + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                        + (duration % (1000 * 60)) / 1000);

//                addItem(new AudioItem("" + id, parentID, title, creator, res));

                //MusicTrack(String id, Container parent, String title, String creator, String album, PersonWithRole artist, Res... resource)
                PersonWithRole personWithRole = new PersonWithRole(creator, "Performer");
                addItem(new MusicTrack(id, parentID, title, creator, album, personWithRole, res));
                ++childCount;

            } while (cursor.moveToNext());
        }

        cursor.close();

        setChildCount(childCount);
        return childCount;
    }

    protected static final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    protected static final String[] columns = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
    };
}
