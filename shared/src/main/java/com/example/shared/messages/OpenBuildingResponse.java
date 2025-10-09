package com.example.shared.messages;

import com.example.shared.model.BuildingType;
import java.io.Serializable;

public class OpenBuildingResponse implements Serializable {
    private boolean ok;
    private String reason;          // якщо !ok
    private String buildingName;
    private BuildingType buildingType;      // якщо ok
    private String errorMessage;

    public OpenBuildingResponse() {
    }

    public OpenBuildingResponse(boolean ok, String errorMessage, String buildingName, BuildingType type) {
        this.ok = ok;
        this.reason = reason;
        this.buildingName = buildingName;
        this.buildingType = type;

    }


    public OpenBuildingResponse(boolean ok, String errorMessage, String reason, String buildingName,
            BuildingType type) {
        this.ok = ok;
        this.reason = reason;
        this.buildingName = buildingName;
        this.buildingType = type;
        this.errorMessage = errorMessage;
    }

    public boolean isOk() {
        return ok;
    }

    public String getReason() {
        return reason;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public BuildingType getType() {
        return buildingType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
