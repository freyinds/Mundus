package com.mbrlabs.mundus.commons.scene3d.components;

import com.badlogic.gdx.utils.Array;
import com.mbrlabs.mundus.commons.assets.TerrainLayerAsset;
import com.mbrlabs.mundus.commons.scene3d.GameObject;

/**
 * Allows you to manage certain aspects of all child terrains that fall under this component.
 * @author JamesTKhan
 * @version July 17, 2023
 */
public class TerrainManagerComponent extends AbstractComponent {

    public TerrainManagerComponent(GameObject go) {
        super(go);
        type = Type.TERRAIN_MANAGER;
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public Component clone(GameObject go) {
        throw new UnsupportedOperationException("Cloning Terrain Manager Not supported.");
    }

    /**
     * Sets the terrain layer asset for all child terrains.
     * @param terrainLayerAsset The terrain layer asset to set.
     * @param modifiedTerrainsOut Optional, The terrains that were modified will be added to this array.
     */
    public void setTerrainLayerAsset(TerrainLayerAsset terrainLayerAsset, Array<TerrainComponent> modifiedTerrainsOut) {
        Array<GameObject> childTerrains = gameObject.findChildrenByComponent(Type.TERRAIN);
        for (GameObject childTerrain : childTerrains) {
            TerrainComponent terrainComponent = (TerrainComponent) childTerrain.findComponentByType(Type.TERRAIN);
            terrainComponent.getTerrainAsset().setTerrainLayerAsset(terrainLayerAsset);
            terrainComponent.applyMaterial();
            if (modifiedTerrainsOut != null) {
                modifiedTerrainsOut.add(terrainComponent);
            }
        }
    }

    /**
     * Sets the triplanar value for all child terrains.
     * @param value The value to set.
     * @param modifiedTerrainsOut  Optional, The terrains that were modified will be added to this array.
     */
    public void setTriplanar(boolean value, Array<TerrainComponent> modifiedTerrainsOut) {
        Array<GameObject> childTerrains = gameObject.findChildrenByComponent(Type.TERRAIN);
        for (GameObject childTerrain : childTerrains) {
            TerrainComponent terrainComponent = (TerrainComponent) childTerrain.findComponentByType(Type.TERRAIN);
            terrainComponent.getTerrainAsset().setTriplanar(value);
            if (modifiedTerrainsOut != null) {
                modifiedTerrainsOut.add(terrainComponent);
            }
        }
    }

}