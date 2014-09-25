package org.oflab.cling.mediaserver.android;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.ProtocolInfos;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.VideoItem;
import org.oflab.cling.mediaserver.android.content.AllImageContainer;
import org.oflab.cling.mediaserver.android.content.AllVideoContainer;
import org.oflab.cling.mediaserver.android.content.BasicContainer;
import org.oflab.cling.mediaserver.android.content.MediaStoreContainer;
import org.oflab.cling.mediaserver.android.mockup.MockupContentDirectoryService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

public class MediaServer implements HttpRequestHandler {

    public MediaServer(Context context) {
        this.context = context;
    }

    public LocalDevice createDevice()
            throws ValidationException, LocalServiceBindingException, IOException {

        // TODO: Not stable
        // Unique device name
        udn = new UDN(UUID.randomUUID());
        // TEST: got from UUID.randomUUID()
        udn = new UDN("571c121c-c10a-428c-8ec1-62d3676c105d");

        // Unique device name, received and offered during discovery with SSDP.
        DeviceIdentity identity = new DeviceIdentity(udn);

        // i.e.   urn:my-domain-namespace:device:MyDevice:1
        DeviceDetails details = new DeviceDetails(
                "Nexus7 (2012)",
                new ManufacturerDetails("oflab"),
                new ModelDetails("oflab MediaServer", "basic mediaserver", "v1")
        );
        DeviceType type = new UDADeviceType("MediaServer", 1);

        AnnotationLocalServiceBinder binder = new AnnotationLocalServiceBinder();

        LocalService[] myLocalServices = new LocalService[]{
                createContentDirectoryService(binder),
                createConnectionManagerService(binder)
        };

        return new LocalDevice(identity, type, details, createDefaultDeviceIcon(), myLocalServices);
    }

    public MediaServer getMediaServer() {
        return this;
    }

    // ContentDirectory
    private LocalService<MockupContentDirectoryService> createMockupContentDirectoryService(
            AnnotationLocalServiceBinder binder) {

        LocalService<MockupContentDirectoryService> contentDirectoryService
                = binder.read(MockupContentDirectoryService.class);

        contentDirectoryService.setManager(new DefaultServiceManager<MockupContentDirectoryService>(
                contentDirectoryService, MockupContentDirectoryService.class));

        return contentDirectoryService;
    }

    // ContentDirectory
    private LocalService<ContentDirectoryService> createContentDirectoryService(
            AnnotationLocalServiceBinder binder) {

        LocalService<ContentDirectoryService> contentDirectoryService
                = binder.read(ContentDirectoryService.class);

        contentDirectoryService.setManager(
                new DefaultServiceManager<ContentDirectoryService>(
                        contentDirectoryService, ContentDirectoryService.class) {
                    @Override
                    protected ContentDirectoryService createServiceInstance() throws Exception {
                        return new ContentDirectoryService(getMediaServer());
                    }
                });

        return contentDirectoryService;
    }

    // ConnectionManager
    private LocalService<ConnectionManagerService> createConnectionManagerService(
            AnnotationLocalServiceBinder binder) {
        LocalService<ConnectionManagerService> connectionManagerService
                = binder.read(ConnectionManagerService.class);

        connectionManagerService.setManager(new DefaultServiceManager<ConnectionManagerService>(
                connectionManagerService, null) {
            @Override
            protected ConnectionManagerService createServiceInstance() throws Exception {
                ProtocolInfos protocolInfos = new ProtocolInfos("http-get:*:video/mpeg:*,http-get:*:audio/mpeg:*");

                return new ConnectionManagerService(protocolInfos, null);
            }
        });

        return connectionManagerService;
    }

    protected Icon createDefaultDeviceIcon() {
        return new Icon(
                "image/png",
                48, 48, 8,
                "icon.png",
                "89504E470D0A1A0A0000000D494844520000002D000000300803000000B28C03ED0000001974455874536F6674" +
                        "776172650041646F626520496D616765526561647971C9653C00000300504C5445EEF2DBAAAAABF5F5" +
                        "F5EEEEF2D1DDBABCC99DCBD2CBC5D0AB88888AFCFCFCD6E6B1CEDAB0BEC9A1F1F1F1F1F5DCDCEAB6D6" +
                        "E2B6D9E4BAB5C78DEAEECCDAE5BCB3BA95D6E1B8DDEEB9C4CCBDB2B3B3CDD9BAE6EEC2F6F6F8D9EAAD" +
                        "DBDDE2EEF3E1C4CEABA4A3A5CDD9AED1D1D1CDDDA9DDECB6D4E3B1DDEDB1656466FAFAFABCBCBCC9D9" +
                        "A6D2DDB5D8E6B4CDDAC2D8E2B9D5DADB899A6BD9DADD9BA684DAEAB0D5E7AACBD6C4F8F8F8C6D2A7EA" +
                        "EDD1CED3D0E8E8E9D4D7D9E6E6E6BFCD9ECAD6ADD5E7ADE2EEBDC6D5A2ECEFD59EA47DDBE9B6CFDCAD" +
                        "ABB594DAECAF7B8D60D1DEC3A6AB81D6E2BEBBCE91B3C696C3C2C3C9C9CAC1CEA1E4E4E9D9E5B6D6E3" +
                        "B4D1D6D4C9D4A9C6CDC1E3F0BCB9C197D2D9D5D1E1AAC7CDC5C9DC9FC6D89DD7E9ADB1B78DE2EEBAE3" +
                        "E3E3D9D8D8EEF1D7D5D5D5A2B28DB9C19AD2E1AE595959C1BFC2E5E6ECCDDEA6C8D3AACAD7A4A2A593" +
                        "D4E0B4FAFAFCB1AFB2D1CED3DEE0E6C5CBC2C9D5BAD4DFB5DEF0B6DFECB7D3E0BED2E0B9CCCCCD4645" +
                        "47EDEDEDD3DEB4DAE8B5D5E0B6D3E4AAD1E2A6D4D2D5D1DDB1E2F2B9C9D3ADD6E1B6CBDAAFC7D0C2C0" +
                        "CBA4D1DCB2B9C29EC8D8A3D4E2AFD3DFB1C5D2A2A7A89DE9E9EFE5F0C0E1E2E9E0EFB5DAEBB5D8E6B2" +
                        "CBDBA7CCD7ADC7D7A5C4CFA6C2CAA0BDC69FBBC69DE5F3BEE4F2BBDDDDDDD7E5B4D8E4B5CFDBB1DAE6" +
                        "B7FFFFFED2DDB3D8D8D7F9F9F9D9E7B5FEFEFDC1D0A7CECACFE0EFBAA0A67FF3F3F6DEEFB0BFCABAD8" +
                        "D4DBD5E1B4AFC08D727173AFC396D9E3BCCBD8AACED9AAD3E0B2D0DFACC2D1A1CDCBD0A1A0A2CFE0A9" +
                        "D7E3B6FBFCFDFBFBFBFDFDFDA5AD90D1D0D49B9F8FD6E1C1B6CA94E0F0B5B7C397677A51EBEDD4EBEE" +
                        "D2C0D392C8CDC7C2D29EB5C2A5BCC6A4D8EAAADBECB3DCEAB3DCDBDEE0E1E6D3DFB3CFDDB6AFABB2C1" +
                        "CEA6E3F4BBA8BB84E1E3EDCCD8B4A4B782D3E6B1A9B086ACB28A3E3D3FBAC79B5F6E4ED3DDB1F0EFF4" +
                        "E6EEC8ECEEF8CBCACEE7EFC4CCD0CD90A473EAEFCECCD6AAE1EDBAE0EEBAE2EFB8A2A39AA6A5A7A1A6" +
                        "8AADAFA6A7AE89A7AC83E9F5C0D9E8B3FEFEFEFFFFFFF96070A50000043A4944415478DA62F84F0A60" +
                        "C022F6AFB0B0F02871AA0FBFB97839CD38CD7D73E846B8EE7F38546B3E30F07B74FF85A7E7D90FDB76" +
                        "18BC81A9FE8755F5E428AF36DFD85843434F4F5F8F53265E72475095A3A89EAA375D7CE1E9B838A10B" +
                        "BEBE57E34CEF557845C9E0545D5276AD627DC4823FBB5959CF6CCB5BB8202242DC2B14453992EAEC32" +
                        "AE6BD3BEB64DF9C8C00702291FA744447CF10AE5C4AA5AA66B2F97FAB4699BDE3933C88301C3F97711" +
                        "EB55F54A900C47A8B661CF70708897DDF4EEBC3303583D83F3C7880AF1E9661BB1A89691D8A592D36C" +
                        "2AEB3145D8D2398501049C85A779C44F9792438439038C715321CCA9AFD9D4D4E3EB9477C2E79D5352" +
                        "9CCF59BEDBE461DAF4D8EA30DC70B8EAD03976817D394DA626AA898ED2D2AF853F9EB7144E549775C8" +
                        "3968F60A6E384CF5D18B779FDB39CD3CD864725DB5F2F3E7C4C408474747759378879CBECE6CB06A64" +
                        "979897769F619DD837339AEBEF75FE4A0850353171689E79296C2A72AA02AB9691E039C31AE8A4BD57" +
                        "6DC66A5757FE4AD5CA4A7E5793C7D12A7D766B4B305C2253CA7366BEDDC4896EFD5CBAAB1B5C41A0E1" +
                        "2F57B4CACC3ED6BB25182E3912D33D9F676EA0859B1A97AEEEEAD50D0D0DAB57EBF6ABA8CC746265C7" +
                        "74F77FAB39DD732C2C3EB94D080959AC0B04AB1B7467F4CFD49EBD8F558219D9251020C7CE6E616F3F" +
                        "8BBB756BC892E0E065CB96E9EACE5053D19E1DD8CDB60A39BC218059A23DD75E4989BB75C2D6254B80" +
                        "EA83972CE652CBE8DC65C16E84129710B0C6AADD7E9612F7D29E5B5B05040E01C19210B53DAD4B95EC" +
                        "BB400EF977142D554D9678C80D54BCA2BE00A81AA43E64EBAD97DC9D5D06CBB1A9FE6F23B114A4B8BC" +
                        "BC430064BA804041797DCF6DAB8D48191949B5F9FBA5F540B58D8D1D62026240E31B1B1BABABD926E3" +
                        "C83BFF9963741AC5C404F4050EE9EBEB83B058355B094A1981928BB3BBAAC5808A0461C0C7CAE8286E" +
                        "D5FFE56258C40405452060A74F958DE67F3CAA35ADA458C4F47742D4FA9CB462C65FB205850395030D" +
                        "DF0934B8AACA660D7ED532A50FCB410EDFA9AF5FC5221544A8D47CCFCEDDA323A62FD658DFF330FC15" +
                        "21D53657D8EDB997AEE8E1569A951B6E4E48B5512FCF9CDCB0B0B05C0BF6EEF0A328C53116D572A2E9" +
                        "F3E602010FCFBCDED2E5F06C80CB6C7751D1C8C833914020FA9E60DD607DECD7D30408703722A8FAE8" +
                        "4F970DDEBF41E049FE2282AA97677DFBE1E2E2B2C1C5F8048732E15A4AAB48EBE7A4FC499314393289" +
                        "507D2093F1EDF1962DFB19356A93D103105375D1330DC6FDC5EBB214336B935769721EC5A3FADFD155" +
                        "C91ADBEF146F29F657D4E048B6DEB88A13A7EA7F4739CDEB8C18398A5A8C6E047CE7D0AA49E235C7A7" +
                        "5A93C95AD99FF100A36411E37749FF54EB8D9AF8541F61AA4B52F697BCC3C858A415906ACB2B73E41F" +
                        "EE30F9C7B98AA9CE7691724D408D726A9235AF0C9A27317CA969CEC46B9B9494646BBD9169D511F496" +
                        "04408001003EE42959E2CD74A60000000049454E44AE426082"
        );
    }

    public UDN getUdn() {
        return udn;
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {
        // TODO:

        String method = httpRequest.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }


        //     '/I406/storage/...'
        String uri = httpRequest.getRequestLine().getUri();
        String objectId;

        // get object's ID
        int pathPos = uri.indexOf('/', 1);
        if (pathPos > 0) {
            objectId = uri.substring(1, pathPos);
        } else {
            Log.w("MediaServer", "Object not found, returning 404");
            httpResponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
            return;
        }

        // get object's file path
        String filePath = Uri.decode(uri.substring(pathPos));
        Log.w("MediaServer", "objectId is " + objectId);

        File file = new File(filePath);
        if (!file.exists()) {
            Log.w("MediaServer", "illegal fileData not readable, returning 404");
            httpResponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
            return;
        }

        InputStream is = new BufferedInputStream(new FileInputStream(new File(filePath)));
        if (is == null) {
            Log.w("MediaServer", "Data not readable, returning 404");
            httpResponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
            return;
        }

        // find the object
        DIDLObject obj = findObjectById(objectId);

        if (obj instanceof ImageItem) {
            ImageItem item = (ImageItem) obj;

            long size = new File(filePath).length();
            InputStreamEntity entity = new InputStreamEntity(is, size);

            Res res = item.getFirstResource();
            entity.setContentType(res.getProtocolInfo().getContentFormat());

            httpResponse.setEntity(entity);
            httpResponse.setStatusCode(HttpStatus.SC_OK);
        } else if (obj instanceof VideoItem) {
            VideoItem item = (VideoItem) obj;

            long size = new File(filePath).length();
            InputStreamEntity entity = new InputStreamEntity(is, size);

            Res res = item.getFirstResource();
            entity.setContentType(res.getProtocolInfo().getContentFormat());

            httpResponse.setEntity(entity);
            httpResponse.setStatusCode(HttpStatus.SC_OK);
        } else {
            Log.w("MediaServer", "Data not readable, returning 404");
            httpResponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
            return;
        }

    }

    public static final String ROOT_ID = "0";
    public static final String ROOT_PARENT_ID = "-1";
    public static final String ROOT_TITLE = "root";
    public static final String AUDIO_ID = "1";
    public static final String IMAGE_ID = "2";
    public static final String VIDEO_ID = "3";
    public static final String ALL_IMAGE_ID = "21";
    public static final String ALL_VIDEO_ID = "31";
    public static final String AUDIO_TITLE = "Music";
    public static final String IMAGE_TITLE = "Images";
    public static final String VIDEO_TITLE = "Video";


    public void loadContainers(String baseUrl) {
        if (rootContainer != null)
            return;

        // TEST
        Log.e("MediaServer", "baseUrl: " + baseUrl);

        rootContainer = new BasicContainer(ROOT_ID, ROOT_PARENT_ID, ROOT_TITLE);

        // image part
        BasicContainer imageRootContainer = new BasicContainer(IMAGE_ID, ROOT_ID, IMAGE_TITLE);
        rootContainer.addContainerAndCount(imageRootContainer);

        MediaStoreContainer allImageContainer = new AllImageContainer(ALL_IMAGE_ID, IMAGE_ID, "All");
        allImageContainer.update(context, baseUrl);
        imageRootContainer.addContainerAndCount(allImageContainer);

        // video part
        BasicContainer videoRootContainer = new BasicContainer(VIDEO_ID, ROOT_ID, VIDEO_TITLE);
        rootContainer.addContainerAndCount(videoRootContainer);

        MediaStoreContainer allVideoContainer = new AllVideoContainer(ALL_VIDEO_ID, VIDEO_ID, "All");
        allVideoContainer.update(context, baseUrl);
        videoRootContainer.addContainerAndCount(allVideoContainer);
    }


    public DIDLObject findObjectById(String id) {
        Container container = rootContainer;

        if (id.equals(container.getId()))
            // found!
            return container;

        return findObjectById(id, container);
    }

    public DIDLObject findObjectById(String id, Container givenContainer) {
        // first, containers
        for (Container container : givenContainer.getContainers()) {
            if (id.equals(container.getId())) {
                // found
                return container;
            }

            DIDLObject object = findObjectById(id, container);
            if (object != null) {
                // found
                return object;
            }
        }

        // seconds, items
        for (Item item : givenContainer.getItems()) {
            if (id.equals(item.getId()))
                // found
                return item;
        }

        // not found
        return null;
    }

    private static final Logger logger = Logger.getLogger(MediaServer.class.getName());
    private BasicContainer rootContainer;
    private Context context;
    private UDN udn;
}
