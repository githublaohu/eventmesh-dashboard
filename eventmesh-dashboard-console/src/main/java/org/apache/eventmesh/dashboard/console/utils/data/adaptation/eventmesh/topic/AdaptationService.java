package org.apache.eventmesh.dashboard.console.utils.data.adaptation.eventmesh.topic;

import org.apache.eventmesh.dashboard.common.enums.ClusterType;
import org.apache.eventmesh.dashboard.common.enums.MetadataType;
import org.apache.eventmesh.dashboard.common.util.ClasspathScanner;
import org.apache.eventmesh.dashboard.console.utils.data.adaptation.Adaptation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author hahaha
 */
public class AdaptationService {

    private static final AdaptationService INSTANCE = new AdaptationService();


    public static AdaptationService getInstance() {
        return INSTANCE;
    }


    static {
        ClasspathScanner scanner = ClasspathScanner.builder().base(Adaptation.class).subPath("/**").interfaceSet(Set.of(Adaptation.class)).build();
        try {
            scanner.getClazz().forEach(INSTANCE::register);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final Map<MetadataType, AdaptationMetadataTypeWrapper> adaptationWrapperHashMap = new HashMap<>();

    private AdaptationService() {
    }

    public AdaptationMetadataTypeWrapper getAdaptationMetadataTypeWrapper(MetadataType metadataType) {
        AdaptationMetadataTypeWrapper wrapper = adaptationWrapperHashMap.get(metadataType);
        if (Objects.isNull(wrapper)) {
            throw new RuntimeException("Not found adaptation for metadata type :" + metadataType);
        }
        return wrapper;
    }

    @SuppressWarnings({"unchecked", "AliDeprecation", "deprecation"})
    private void register(Class<?> clazz) {
        try {
            String name = clazz.getSimpleName();
            if (name.endsWith("Proxy")) {
                return;
            }

            Adaptation<Object, Object> adaptation = (Adaptation<Object, Object>) clazz.newInstance();
            AdaptationMetadataTypeWrapper wrapper =
                adaptationWrapperHashMap.computeIfAbsent(adaptation.metadataType(), (k) -> new AdaptationMetadataTypeWrapper());
            wrapper.register(adaptation);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static class AdaptationMetadataTypeWrapper {

        private final Map<ClusterType /* ClusterType */, Map<String, Adaptation<Object, Object>>> clusterTypeMapMap = new HashMap<>();


        public void register(Adaptation<Object, Object> adaptation) {
            for (ClusterType clusterType : adaptation.clusterType()) {
                clusterTypeMapMap.computeIfAbsent(clusterType, k -> new HashMap<>()).put(adaptation.operation(), this.createProxy(adaptation));
            }
        }

        public Adaptation<Object, Object> get(ClusterType clusterType, String operation) {
            return this.clusterTypeMapMap.get(clusterType).get(operation);
        }

        private Adaptation<Object, Object> createProxy(Adaptation<Object, Object> adaptation) {
            AdaptationProxy adaptationProxy = new AdaptationProxy();
            adaptationProxy.adaptation = adaptation;
            return adaptationProxy;
        }
    }

    private static class AdaptationProxy implements Adaptation<Object, Object> {

        private Adaptation<Object, Object> adaptation;

        @Override
        public ClusterType[] clusterType() {
            return adaptation.clusterType();
        }

        @Override
        public String operation() {
            return adaptation.operation();
        }

        @Override
        public MetadataType metadataType() {
            return adaptation.metadataType();
        }

        @Override
        public void adaptation(Object o, Object o2) {
            adaptation.adaptation(o, o2);
            adaptation.fill(o);
        }

        @Override
        public void fill(Object o) {
            adaptation.fill(o);
        }
    }
}
