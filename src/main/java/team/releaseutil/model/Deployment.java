package team.releaseutil.model;

import java.util.Date;
import java.util.HashSet;

public class Deployment
    {
    private final String deploymentId;
    private final String deploymentDesc;
    private final String applicationId;
    private final String rfcId;
    private final Date created;
    private final String source;
    private final HashSet<Change> changes;

    public Deployment(String deploymentId, String deploymentDesc, String applicationId, String rfcId, Date created, String source, HashSet<Change> changes)
        {
        this.deploymentId = deploymentId;
        this.deploymentDesc = deploymentDesc;
        this.applicationId = applicationId;
        this.rfcId = rfcId;
        this.created = created;
        this.source = source;
        this.changes = changes;
        }

    public String getDeploymentId()
        {
        return deploymentId;
        }

    public String getDeploymentDesc() { return deploymentDesc; }

    public String getApplicationId()
        {
        return applicationId;
        }

    public String getRfcId() { return rfcId; }
    
    public Date getCreated()
        {
        return created;
        }

    public String getSource()
        {
        return source;
        }

    public HashSet<Change> getChanges()
        {
        return changes;
        }

    @Override
    public String toString()
        {
        return "Deployment{" +
            "deploymentId='" + deploymentId + '\'' +
            ", deploymentDesc='" + deploymentDesc + '\'' +
            ", applicationId='" + applicationId + '\'' +
            ", rfcId='" + rfcId + '\'' +
            ", created=" + created +
            ", source='" + source + '\'' +
            ", changes=" + changes +
            '}';
        }
    }
