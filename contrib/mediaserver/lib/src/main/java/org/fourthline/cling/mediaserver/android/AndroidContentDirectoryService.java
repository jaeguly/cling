package org.fourthline.cling.mediaserver.android;

import android.util.Log;

import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.StorageFolder;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.fourthline.cling.support.model.item.Photo;
import org.seamless.util.MimeType;

import java.util.ArrayList;
import java.util.List;

public class AndroidContentDirectoryService extends AbstractContentDirectoryService {


    @Override
    public BrowseResult browse(String objectID, BrowseFlag browseFlag, String filter,
                               long firstResult, long maxResults, SortCriterion[] orderBy)
            throws ContentDirectoryException {
        DIDLContent didlContent = new DIDLContent();

        Log.i(TAG, "browse(objectID:" + objectID + ", browseFlag:" + browseFlag
                + ", filter:" + filter + ", firstResult:" + firstResult
                + ", maxResults:" + maxResults + ", orderby:" + orderBy);

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

        } else if ("0".equalsIgnoreCase(objectID)) {

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

        } else if ("1".equalsIgnoreCase(objectID)) {

            List<MusicTrack> tracks = createMusicTracks("1");

            for (MusicTrack track : tracks) {
                didlContent.addItem(track);
            }

            resultCount = tracks.size();

        } else if ("2".equalsIgnoreCase(objectID)) {

            List<Photo> photos = createPhotos("2");

            for (Photo photo : photos) {
                didlContent.addItem(photo);
            }

            resultCount = photos.size();
        }

        try {
            String didlString = new DIDLParser().generate(didlContent);
            return new BrowseResult(didlString, resultCount, resultCount);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<MusicTrack> createMusicTracks(String parentId) {
        List<MusicTrack> result = new ArrayList<MusicTrack>();

        String album = "Music";
        String creator = "Unknown";
        PersonWithRole artist = new PersonWithRole(creator, "");
        MimeType mimeType = new MimeType("audio", "mpeg");
        MusicTrack musicTrack = new MusicTrack(
                "101", parentId, "Track 1", creator, album, artist,
                new Res(mimeType, 123456l, "00:02:33", 8192L,
                        "http://api.jamendo.com/get2/stream/track/redirect/?id=310355&streamencoding=mp31"));
        musicTrack.setRestricted(true);
        result.add(musicTrack);

        musicTrack = new MusicTrack(
                "102", parentId, "Track 2", creator, album, artist,
                new Res(mimeType, 123456l, "00:02:01", 8192L,
                        "http://api.jamendo.com/get2/stream/track/redirect/?id=310370&streamencoding=mp31"));
        musicTrack.setRestricted(true);
        result.add(musicTrack);

        return result;
    }

    private List<Photo> createPhotos(String parentId) {
        List<Photo> result = new ArrayList<Photo>();

        String album = null;
        String creator = null;
        MimeType mimeType = new MimeType("image", "jpg");

        String url = "https://a-v2.sndcdn.com/assets/images/front/zeds_dead@2x-0107f5.jpg";
        Photo photo = new Photo("201", parentId, url, creator, album,
                new Res(mimeType, 123456L, url));
        photo.setRestricted(true);
        photo.setClazz(new DIDLObject.Class("object.item.imageItem"));
        result.add(photo);

        url = "https://a-v2.sndcdn.com/assets/images/front/little_simz@2x-30fac7.jpg";
        photo = new Photo("202", parentId, url, creator, album, new Res(
                mimeType, 123456L, url));
        photo.setRestricted(true);
        photo.setClazz(new DIDLObject.Class("object.item.imageItem"));
        result.add(photo);

        return result;
    }

    @Override
    public BrowseResult search(String containerId, String searchCriteria, String filter, long firstResult,
                               long maxResults, SortCriterion[] orderBy) throws ContentDirectoryException {
        Log.i("MyContentDirectoryService", "search(): containerId = " + containerId + ", searchCriteria = "
                + searchCriteria);

        return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
    }

    private static final String TAG = AndroidContentDirectoryService.class.getSimpleName();
}
