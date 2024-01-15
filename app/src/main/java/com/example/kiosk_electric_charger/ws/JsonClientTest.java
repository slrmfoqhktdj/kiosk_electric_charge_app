package com.example.kiosk_electric_charger.ws;

import static eu.chargetime.ocpp.JSONConfiguration.CONNECT_TIMEOUT_IN_MS_PARAMETER;
import static eu.chargetime.ocpp.JSONConfiguration.PING_INTERVAL_PARAMETER;

import com.example.kiosk_electric_charger.model.DeviceModel;

import eu.chargetime.ocpp.*;
import eu.chargetime.ocpp.feature.profile.ClientCoreProfile;
import eu.chargetime.ocpp.feature.profile.ClientFirmwareManagementProfile;
import eu.chargetime.ocpp.feature.profile.Profile;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.test.TestSessionFactory;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.protocols.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonClientTest implements IClientAPI {

    private static final Logger logger = LoggerFactory.getLogger(JsonClientTest.class);
    private static final String identity = DeviceModel.getDeviceid();

    public final Draft draftOcppOnly;
    private final WebSocketTransmitter transmitter;
    private final FeatureRepository featureRepository;
    private final Client client;

    public JsonClientTest (ClientCoreProfile coreProfile, ClientFirmwareManagementProfile firmwareProfile) {
        draftOcppOnly =
                new Draft_6455(Collections.emptyList(), Collections.singletonList(new Protocol("ocpp1.6")));
        // pingpong 20초 설정
        transmitter = new WebSocketTransmitter(JSONConfiguration.get().setParameter(PING_INTERVAL_PARAMETER, 20).setParameter(CONNECT_TIMEOUT_IN_MS_PARAMETER,0), draftOcppOnly);
        JSONCommunicator communicator = new JSONCommunicator(transmitter);
        featureRepository = new FeatureRepository();
        ISession session = new TestSessionFactory(featureRepository).createSession(communicator);
        client = new Client(session, featureRepository,new PromiseRepository());
        featureRepository.addFeatureProfile(coreProfile);
        featureRepository.addFeatureProfile(firmwareProfile);





    }



    @Override
    public void addFeatureProfile(Profile profile) {
        featureRepository.addFeatureProfile(profile);
    }

    @Override
    public void connect(String url, ClientEvents clientEvents) {
        logger.debug("Feature repository: {}", featureRepository);

        String identityUrl = String.format("%s/%s", url, identity);
        client.connect(identityUrl, clientEvents);
    }


    @Override
    public CompletionStage<Confirmation> send(Request request) throws OccurenceConstraintException, UnsupportedFeatureException {
        return client.send(request);
    }


    @Override
    public void disconnect() {
        client.disconnect();
    }

    @Override
    public boolean isClosed() {
        return transmitter.isClosed();
    }

    @Override
    public UUID getSessionId() {
        return client.getSessionId();
    }
}
