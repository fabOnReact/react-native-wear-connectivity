package com.wearconnectivity;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class WearConnectivityModuleInstrumentedTest {

    private ReactApplicationContext reactContext;
    private WearConnectivityModule module;
    private MockedStatic<Wearable> wearableStatic;
    private MockedStatic<GoogleApiAvailability> googleApiAvailabilityStatic;
    private MessageClient messageClient;
    private DataClient dataClient;
    private NodeClient nodeClient;
    private GoogleApiAvailability googleApiAvailability;

    @Before
    public void setUp() {
        Application application = ApplicationProvider.getApplicationContext();
        reactContext = new ReactApplicationContext(application);

        messageClient = mock(MessageClient.class);
        dataClient = mock(DataClient.class);
        nodeClient = mock(NodeClient.class);
        googleApiAvailability = mock(GoogleApiAvailability.class);

        wearableStatic = Mockito.mockStatic(Wearable.class);
        wearableStatic.when(() -> Wearable.getMessageClient(any())).thenReturn(messageClient);
        wearableStatic.when(() -> Wearable.getDataClient(any())).thenReturn(dataClient);
        wearableStatic.when(() -> Wearable.getNodeClient(any())).thenReturn(nodeClient);

        when(messageClient.addListener(any(MessageClient.OnMessageReceivedListener.class)))
                .thenReturn(Tasks.forResult(null));
        when(dataClient.addListener(any(DataClient.OnDataChangedListener.class)))
                .thenReturn(Tasks.forResult(null));

        googleApiAvailabilityStatic = Mockito.mockStatic(GoogleApiAvailability.class);
        googleApiAvailabilityStatic.when(GoogleApiAvailability::getInstance).thenReturn(googleApiAvailability);
        when(googleApiAvailability.isGooglePlayServicesAvailable(any()))
                .thenReturn(ConnectionResult.SUCCESS);
        when(googleApiAvailability.checkApiAvailability(any()))
                .thenReturn(Tasks.forResult(null));

        module = new WearConnectivityModule(reactContext);
    }

    @After
    public void tearDown() {
        googleApiAvailabilityStatic.close();
        wearableStatic.close();
    }

    @Test
    public void sendMessage_usesNearbyNode() throws Exception {
        Node nearbyNode = mock(Node.class);
        when(nearbyNode.isNearby()).thenReturn(true);
        when(nearbyNode.getId()).thenReturn("node-1");
        when(nodeClient.getConnectedNodes()).thenReturn(Tasks.forResult(Collections.singletonList(nearbyNode)));
        when(messageClient.sendMessage(eq("node-1"), anyString(), isNull()))
                .thenAnswer(invocation -> Tasks.forResult(1));

        Callback replyCb = mock(Callback.class);
        Callback errorCb = mock(Callback.class);
        WritableMap message = Arguments.createMap();
        message.putString("event", "greeting");
        message.putString("payload", "hello");

        module.sendMessage(message, replyCb, errorCb);

        verify(messageClient, timeout(1000))
                .sendMessage(eq("node-1"), argThat(json -> json.contains("\"event\":\"greeting\"")), isNull());
        verify(replyCb, timeout(1000)).invoke(startsWith("message sent to client"));
        verifyNoInteractions(errorCb);
    }

    @Test
    public void sendMessage_withoutNodes_callsErrorCallback() throws Exception {
        when(nodeClient.getConnectedNodes()).thenReturn(Tasks.forResult(Collections.emptyList()));

        Callback replyCb = mock(Callback.class);
        Callback errorCb = mock(Callback.class);
        WritableMap message = Arguments.createMap();

        module.sendMessage(message, replyCb, errorCb);

        verify(errorCb).invoke(contains("No connected nodes"));
        verifyNoInteractions(replyCb);
    }

    @Test
    public void sendFile_putsAssetAndResolvesPromise() throws Exception {
        File file = File.createTempFile("wear-connectivity", ".txt", applicationFilesDir());
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write("hello".getBytes(StandardCharsets.UTF_8));
        }

        ArgumentCaptor<PutDataRequest> requestCaptor = ArgumentCaptor.forClass(PutDataRequest.class);
        when(dataClient.putDataItem(requestCaptor.capture()))
                .thenAnswer(invocation -> Tasks.forResult(mock(DataItem.class)));

        Promise promise = mock(Promise.class);

        module.sendFile(file.getAbsolutePath(), Arguments.createMap(), promise);

        PutDataRequest request = requestCaptor.getValue();
        assertEquals("/file_transfer", request.getUri().getPath());
        verify(promise, timeout(1000)).resolve("File sent successfully via DataClient.");
        verifyNoMoreInteractions(promise);

        // Clean up the temporary file
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private File applicationFilesDir() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext().getFilesDir();
    }
}
