package team.releaseutil.model;

import java.util.Date;
import java.util.Objects;

public class Change
    {
    private final String id;
    private final Date created;
    private final String source;
    private final String eventType;

    public Change(String id, Date created, String source, String eventType)
        {
        this.id = id;
        this.created = created;
        this.source = source;
        this.eventType = eventType;
        }

    public String getId()
        {
        return id;
        }

    public Date getCreated()
        {
        return created;
        }

    public String getSource()
        {
        return source;
        }

    public String getEventType()
        {
        return eventType;
        }

    @Override
    public boolean equals(Object o)
        {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Change change = (Change) o;
        return Objects.equals(id, change.id) &&
                Objects.equals(created, change.created) &&
                Objects.equals(source, change.source) &&
                Objects.equals(eventType, change.eventType);
        }

    @Override
    public int hashCode()
        {
        return Objects.hash(id, created, source, eventType);
        }

    @Override
    public String toString()
        {
        return "Change{" +
            "id='" + id + '\'' +
            ", created=" + created +
            ", source='" + source + '\'' +
            ", eventType='" + eventType + '\'' +
            '}';
        }
    }
