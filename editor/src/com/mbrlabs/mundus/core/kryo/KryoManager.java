package com.mbrlabs.mundus.core.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.mbrlabs.mundus.core.Files;
import com.mbrlabs.mundus.core.kryo.descriptors.*;
import com.mbrlabs.mundus.core.project.ProjectContext;
import com.mbrlabs.mundus.core.project.ProjectRef;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Marcus Brummer
 * @version 12-12-2015
 */
public class KryoManager {

    private Kryo kryo;

    public KryoManager() {
        kryo = new Kryo();
        kryo.setDefaultSerializer(TaggedFieldSerializer.class);
        // !!!!! DO NOT CHANGE THIS, OTHERWISE ALREADY SERIALIZED OBJECTS WILL BE UNREADABLE !!!!
        kryo.register(ArrayList.class, 0);
        kryo.register(Date.class, 1);
        kryo.register(ProjectRef.class, 2);
        kryo.register(HomeDescriptor.class, 3);
        kryo.register(HomeDescriptor.Settings.class, 4);
        kryo.register(ProjectDescriptor.class, 5);
        kryo.register(TerrainDescriptor.class, 6);
        kryo.register(ModelDescriptor.class, 7);
    }

    public HomeDescriptor loadHomeDescriptor() {
        try {
            Input input = new Input(new FileInputStream(Files.HOME_DATA_FILE));
            HomeDescriptor homeDescriptor = kryo.readObjectOrNull(input, HomeDescriptor.class);
            if(homeDescriptor == null) {
                homeDescriptor = new HomeDescriptor();
            }
            return homeDescriptor;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new HomeDescriptor();
    }

    public void saveHomeDescriptor(HomeDescriptor homeDescriptor) {
        try {
            Output output = new Output(new FileOutputStream(Files.HOME_DATA_FILE));
            kryo.writeObject(output, homeDescriptor);
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveProjectContext(ProjectContext context) {
        try {
            Output output = new Output(new FileOutputStream(context.path + "/" + context.name + ".mundus"));

            ProjectDescriptor descriptor = DescriptorConverter.convert(context);
            kryo.writeObject(output, descriptor);

            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ProjectContext loadProjectContext(ProjectRef ref) {
        try {
            Input input = new Input(new FileInputStream(ref.getPath() + "/" + ref.getName() + ".mundus"));
            ProjectDescriptor projectDescriptor = kryo.readObjectOrNull(input, ProjectDescriptor.class);
            return DescriptorConverter.convert(projectDescriptor);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new ProjectContext(-1);
    }



}
