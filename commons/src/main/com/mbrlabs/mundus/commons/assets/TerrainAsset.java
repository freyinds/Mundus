/*
 * Copyright (c) 2016. See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mbrlabs.mundus.commons.assets;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mbrlabs.mundus.commons.assets.meta.Meta;
import com.mbrlabs.mundus.commons.terrain.SplatMap;
import com.mbrlabs.mundus.commons.terrain.SplatTexture;
import com.mbrlabs.mundus.commons.terrain.Terrain;
import com.mbrlabs.mundus.commons.terrain.TerrainLoader;
import com.mbrlabs.mundus.commons.terrain.TerrainMaterial;

import java.util.Map;

/**
 * @author Marcus Brummer
 * @version 01-10-2016
 */
public class TerrainAsset extends Asset {

    private float[] data;

    // dependencies
    private PixmapTextureAsset splatmap;
    private TerrainLayerAsset terrainLayerAsset;
    private MaterialAsset materialAsset;

    private Terrain terrain;

    public TerrainAsset(Meta meta, FileHandle assetFile) {
        super(meta, assetFile);
    }

    public float[] getData() {
        return data;
    }

    public PixmapTextureAsset getSplatmap() {
        return splatmap;
    }

    public void setSplatmap(PixmapTextureAsset splatmap) {
        this.splatmap = splatmap;
        if (splatmap == null) {
            meta.getTerrain().setSplatmap(null);
        } else {
            meta.getTerrain().setSplatmap(splatmap.getID());
        }
    }

    public void setTriplanar(boolean value) {
        meta.getTerrain().setTriplanar(value);
        terrain.getTerrainTexture().setTriplanar(value);
    }

    public Terrain getTerrain() {
        return terrain;
    }

    @Override
    public void load() {
        // Load a terrain synchronously
        FileHandle terraFile = getTerraFile();
        TerrainLoader.TerrainParameter param = new TerrainLoader.TerrainParameter(meta.getTerrain());
        TerrainLoader terrainLoader = new TerrainLoader(null);
        terrainLoader.loadAsync(null, null, terraFile, param);
        terrain = terrainLoader.loadSync(null, null, terraFile, param);
        setTriplanar(meta.getTerrain().isTriplanar());
    }

    public TerrainLoader startAsyncLoad() {
        TerrainLoader.TerrainParameter param = new TerrainLoader.TerrainParameter(meta.getTerrain());
        TerrainLoader terrainLoader = new TerrainLoader(null);
        terrainLoader.loadAsync(null, null, getTerraFile(), param);
        return terrainLoader;
    }

    public void finishSyncLoad(TerrainLoader terrainLoader) {
        TerrainLoader.TerrainParameter param = new TerrainLoader.TerrainParameter(meta.getTerrain());
        terrain = terrainLoader.loadSync(null, null, getTerraFile(), param);
        setTriplanar(meta.getTerrain().isTriplanar());
    }

    @Override
    public void load(AssetManager assetManager) {
        terrain = assetManager.get(meta.getFile().pathWithoutExtension());
        setTriplanar(meta.getTerrain().isTriplanar());
        data = terrain.heightData;
    }

    public FileHandle getTerraFile() {
        FileHandle terraFile;
        if (meta.getFile().type() == Files.FileType.Absolute) {
            terraFile = Gdx.files.absolute(meta.getFile().pathWithoutExtension());
        } else {
            terraFile = Gdx.files.internal(meta.getFile().pathWithoutExtension());
        }
        return terraFile;
    }

    @Override
    public void resolveDependencies(Map<String, Asset> assets) {
        // terrain layer
        if (assets.containsKey(meta.getTerrain().getTerrainLayerAssetId())) {
            terrainLayerAsset = (TerrainLayerAsset) assets.get(meta.getTerrain().getTerrainLayerAssetId());
        } else {
            throw new GdxRuntimeException("Cannot find a TerrainLayerAsset for asset: " + getName() +
                    ". A terrain layer can be generated automatically by opening the project in the editor.");
        }

        // material
        String materialId = meta.getTerrain().getMaterialId();
        if (materialId == null || materialId.isEmpty()) {
            materialId = "terrain_default";
            meta.getTerrain().setMaterialId(materialId);
        }

        if (assets.containsKey(materialId)) {
            MaterialAsset asset = (MaterialAsset) assets.get(materialId);
            setMaterialAsset(asset);
        } else {
            Gdx.app.error("TerrainAsset", "Cannot find material asset " + materialId +
                    ". A default material can be generated by opening the project in the editor.");
        }

        // splatmap
        String id = meta.getTerrain().getSplatmap();
        if (id != null && assets.containsKey(id)) {
            setSplatmap((PixmapTextureAsset) assets.get(id));

            // If WebGL, we use base64 string for pixmap since pixmap cannot read from binaries on GWT
            if (Gdx.app.getType() == Application.ApplicationType.WebGL) {
                ((PixmapTextureAsset) assets.get(id)).loadBase64(meta.getTerrain().getSplatBase64());
            }

        }
    }

    @Override
    public void applyDependencies() {
        TerrainMaterial terrainMaterial = terrain.getTerrainTexture();

        if (materialAsset != null) {
            materialAsset.applyToMaterial(terrain.getMaterial(), true);
        }

        if (splatmap == null) {
            terrainMaterial.setSplatmap(null);
        } else {
            terrainMaterial.setSplatmap(new SplatMap(splatmap));
        }
        if (terrainLayerAsset.getSplatBase() == null) {
            terrainMaterial.removeTexture(SplatTexture.Channel.BASE);
        } else {
            terrainMaterial.setSplatTexture(new SplatTexture(SplatTexture.Channel.BASE, terrainLayerAsset.getSplatBase()));
        }
        if (terrainLayerAsset.getSplatR() == null) {
            terrainMaterial.removeTexture(SplatTexture.Channel.R);
        } else {
            terrainMaterial.setSplatTexture(new SplatTexture(SplatTexture.Channel.R, terrainLayerAsset.getSplatR()));
        }
        if (terrainLayerAsset.getSplatG() == null) {
            terrainMaterial.removeTexture(SplatTexture.Channel.G);
        } else {
            terrainMaterial.setSplatTexture(new SplatTexture(SplatTexture.Channel.G, terrainLayerAsset.getSplatG()));
        }
        if (terrainLayerAsset.getSplatB() == null) {
            terrainMaterial.removeTexture(SplatTexture.Channel.B);
        } else {
            terrainMaterial.setSplatTexture(new SplatTexture(SplatTexture.Channel.B, terrainLayerAsset.getSplatB()));
        }
        if (terrainLayerAsset.getSplatA() == null) {
            terrainMaterial.removeTexture(SplatTexture.Channel.A);
        } else {
            terrainMaterial.setSplatTexture(new SplatTexture(SplatTexture.Channel.A, terrainLayerAsset.getSplatA()));
        }

        if (terrainLayerAsset.getSplatBaseNormal() == null) {
            terrainMaterial.removeNormalTexture(SplatTexture.Channel.BASE);
        } else {
            terrainMaterial.setSplatNormalTexture(new SplatTexture(SplatTexture.Channel.BASE, terrainLayerAsset.getSplatBaseNormal()));
        }
        if (terrainLayerAsset.getSplatRNormal() == null) {
            terrainMaterial.removeNormalTexture(SplatTexture.Channel.R);
        } else {
            terrainMaterial.setSplatNormalTexture(new SplatTexture(SplatTexture.Channel.R, terrainLayerAsset.getSplatRNormal()));
        }
        if (terrainLayerAsset.getSplatGNormal() == null) {
            terrainMaterial.removeNormalTexture(SplatTexture.Channel.G);
        } else {
            terrainMaterial.setSplatNormalTexture(new SplatTexture(SplatTexture.Channel.G, terrainLayerAsset.getSplatGNormal()));
        }
        if (terrainLayerAsset.getSplatBNormal() == null) {
            terrainMaterial.removeNormalTexture(SplatTexture.Channel.B);
        } else {
            terrainMaterial.setSplatNormalTexture(new SplatTexture(SplatTexture.Channel.B, terrainLayerAsset.getSplatBNormal()));
        }
        if (terrainLayerAsset.getSplatANormal() == null) {
            terrainMaterial.removeNormalTexture(SplatTexture.Channel.A);
        } else {
            terrainMaterial.setSplatNormalTexture(new SplatTexture(SplatTexture.Channel.A, terrainLayerAsset.getSplatANormal()));
        }

        terrain.update();
    }

    @Override
    public void dispose() {

    }

    public void updateUvScale(Vector2 uvScale) {
        terrain.updateUvScale(uvScale);
        terrain.update();
        meta.getTerrain().setUv(uvScale.x);
    }

    public TerrainLayerAsset getTerrainLayerAsset() {
        return terrainLayerAsset;
    }

    public MaterialAsset getMaterialAsset() {
        return materialAsset;
    }

    public void setMaterialAsset(MaterialAsset materialAsset) {
        this.materialAsset = materialAsset;
        meta.getTerrain().setMaterialId(materialAsset.getID());
    }

    @Override
    public boolean usesAsset(Asset assetToCheck) {
        if (assetToCheck == splatmap)
            return true;

        if (assetToCheck == materialAsset)
            return true;

        if (assetToCheck == terrainLayerAsset)
            return true;

        // does the splatmap use the asset
        if (assetToCheck instanceof TextureAsset) {
            for (Map.Entry<SplatTexture.Channel, SplatTexture> texture : terrain.getTerrainTexture().getTextures().entrySet()) {
                if (texture.getValue().texture.getFile().path().equals(assetToCheck.getFile().path())) {
                    return true;
                }
            }

            return terrainLayerAsset.usesAsset(assetToCheck);
        }

        return false;
    }
}
