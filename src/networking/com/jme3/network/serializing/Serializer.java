package com.jme3.network.serializing;

import com.jme3.network.message.*;
import com.jme3.network.serializing.serializers.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Logger;

/**
 * The main serializer class, which will serialize objects such that
 *  they can be sent across the network. Serializing classes should extend
 *  this to provide their own serialization.
 *
 * @author Lars Wesselius
 */
public abstract class Serializer {
    protected static final Logger log = Logger.getLogger(Serializer.class.getName());

    private static final Map<Short, SerializerRegistration> idRegistrations         = new HashMap<Short, SerializerRegistration>();
    private static final Map<Class, SerializerRegistration> classRegistrations      = new HashMap<Class, SerializerRegistration>();

    private static final Serializer                         fieldSerializer         = new FieldSerializer();
    private static final Serializer                         serializableSerializer  = new SerializableSerializer();
    private static final Serializer                         arraySerializer         = new ArraySerializer();

    private static short                                    nextId                  = 0;



    // Registers the classes we already have serializers for.
    static {
        registerClass(boolean.class,   new BooleanSerializer());
        registerClass(byte.class,      new ByteSerializer());
        registerClass(char.class,      new CharSerializer());
        registerClass(short.class,     new ShortSerializer());
        registerClass(int.class,       new IntSerializer());
        registerClass(long.class,      new LongSerializer());
        registerClass(float.class,     new FloatSerializer());
        registerClass(double.class,    new DoubleSerializer());

        registerClass(Boolean.class,   new BooleanSerializer());
        registerClass(Byte.class,      new ByteSerializer());
        registerClass(Character.class, new CharSerializer());
        registerClass(Short.class,     new ShortSerializer());
        registerClass(Integer.class,   new IntSerializer());
        registerClass(Long.class,      new LongSerializer());
        registerClass(Float.class,     new FloatSerializer());
        registerClass(Double.class,    new DoubleSerializer());
        registerClass(String.class,    new StringSerializer());

        registerClass(Date.class,      new DateSerializer());
        registerClass(Collection.class, new CollectionSerializer());
        registerClass(GZIPCompressedMessage.class, new GZIPSerializer());
        registerClass(ZIPCompressedMessage.class, new ZIPSerializer());

        registerClass(Message.class);
        registerClass(DisconnectMessage.class);
        registerClass(ClientRegistrationMessage.class);
        registerClass(DiscoverHostMessage.class);

    }

    public static SerializerRegistration registerClass(Class cls) {
        if (cls.isAnnotationPresent(Serializable.class)) {
            Serializable serializable = (Serializable)cls.getAnnotation(Serializable.class);

            Class serializerClass = serializable.serializer();
            short classId = serializable.id();
            if (classId == 0) classId = --nextId;

            Serializer serializer = getSerializer(serializerClass);

            if (serializer == null) serializer = fieldSerializer;

            SerializerRegistration existingReg = getExactSerializerRegistration(cls);

            if (existingReg != null) classId = existingReg.getId();
            SerializerRegistration reg = new SerializerRegistration(serializer, cls, classId);

            idRegistrations.put(classId, reg);
            classRegistrations.put(cls, reg);

            serializer.initialize(cls);

            return reg;
        }
        return null;
    }

    public static SerializerRegistration[] registerPackage(String pkgName) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = pkgName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            List<File> dirs = new ArrayList<File>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            ArrayList<Class> classes = new ArrayList<Class>();
            for (File directory : dirs) {
                classes.addAll(findClasses(directory, pkgName));
            }

            SerializerRegistration[] registeredClasses = new SerializerRegistration[classes.size()];
            for (int i = 0; i != classes.size(); ++i) {
                Class clz = classes.get(i);
                registeredClasses[i] = registerClass(clz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SerializerRegistration[0];
    }

    private static List<Class> findClasses(File dir, String pkgName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!dir.exists()) {
            return classes;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, pkgName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(pkgName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    public static SerializerRegistration registerClass(Class cls, Serializer serializer) {
        SerializerRegistration existingReg = getExactSerializerRegistration(cls);

        short id = --nextId;
        if (existingReg != null) id = existingReg.getId();
        SerializerRegistration reg = new SerializerRegistration(serializer, cls, id);

        idRegistrations.put(id, reg);
        classRegistrations.put(cls, reg);

        serializer.initialize(cls);

        return reg;
    }

    public static Serializer getExactSerializer(Class cls) {
        return classRegistrations.get(cls).getSerializer();
    }

    public static Serializer getSerializer(Class cls) {
        return getSerializerRegistration(cls).getSerializer();
    }

    public static SerializerRegistration getExactSerializerRegistration(Class cls) {
        return classRegistrations.get(cls);
    }

    public static SerializerRegistration getSerializerRegistration(Class cls) {
        SerializerRegistration reg = classRegistrations.get(cls);

        if (reg != null) return reg;

        for (Map.Entry<Class, SerializerRegistration> entry : classRegistrations.entrySet()) {
            if (entry.getKey().isAssignableFrom(Serializable.class)) continue;
            if (entry.getKey().isAssignableFrom(cls)) return entry.getValue();
        }

        if (cls.isArray()) return registerClass(cls, arraySerializer);

        if (Serializable.class.isAssignableFrom(cls)) return getExactSerializerRegistration(java.io.Serializable.class);
        return registerClass(cls, fieldSerializer);
    }


    ///////////////////////////////////////////////////////////////////////////////////


    /**
     * Read the class from given buffer and return its SerializerRegistration.
     *
     * @param buffer The buffer to read from.
     * @return The SerializerRegistration, or null if non-existant.
     */
    public static SerializerRegistration readClass(ByteBuffer buffer) {
        return idRegistrations.get(buffer.getShort());
    }

    /**
     * Read the class and the object.
     *
     * @param buffer Buffer to read from.
     * @return The Object that was read.
     * @throws IOException If serialization failed.
     */
    public static Object readClassAndObject(ByteBuffer buffer) throws IOException {
        SerializerRegistration reg = readClass(buffer);
        return reg.getSerializer().readObject(buffer, reg.getType());
    }

    /**
     * Write a class and return its SerializerRegistration.
     *
     * @param buffer The buffer to write the given class to.
     * @param type The class to write.
     * @return The SerializerRegistration that's registered to the class.
     */
    public static SerializerRegistration writeClass(ByteBuffer buffer, Class type) {
        SerializerRegistration reg = getSerializerRegistration(type);
        if (reg == null) {
            reg = classRegistrations.get(Message.class);
            //registerClassToSerializer(type, FieldSerializer.class);
        }
        buffer.putShort(reg.getId());
        return reg;
    }

    /**
     * Write the class and object.
     *
     * @param buffer The buffer to write to.
     * @param object The object to write.
     * @throws IOException If serializing fails.
     */
    public static void writeClassAndObject(ByteBuffer buffer, Object object) throws IOException {
        SerializerRegistration reg = writeClass(buffer, object.getClass());
        reg.getSerializer().writeObject(buffer, object);
    }

    /**
     * Read an object from the buffer, effectively deserializing it.
     *
     * @param data The buffer to read from.
     * @param c The class of the object.
     * @return The object read.
     * @throws IOException If deserializing fails.
     */
    public abstract <T> T readObject(ByteBuffer data, Class<T> c) throws IOException;

    /**
     * Write an object to the buffer, effectively serializing it.
     *
     * @param buffer The buffer to write to.
     * @param object The object to serialize.
     * @throws IOException If serializing fails.
     */
    public abstract void writeObject(ByteBuffer buffer, Object object) throws IOException;

    /**
     * Registration for when a serializer may need to cache something.
     *
     * Override to use.
     *
     * @param clazz The class that has been registered to the serializer.
     */
    public void initialize(Class clazz) { };
}
