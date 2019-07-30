package com.data.collection.data.greendao;

import com.data.collection.util.DateUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;

import java.io.Serializable;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;


@Entity
public class Ploygon implements Serializable{
    private static final long serialVersionUID = 10L;

    private static final String TAG = "Ploygon";
    @Id(autoincrement = true)
    private Long id; // 本地数据库 id 1

    private String name;

    private boolean isLine = true;

    @ToMany(referencedJoinProperty = "ploygonId")
    private List<SimplePoint> points;

    @NotNull
    private int pointCount;

    private String  comments; // 备注

    private double measureResult; // 测量结果，平方米或者米

    @NotNull
    private String time = DateUtils.getNow(DateUtils.fmtYYYYMMDDhhmmss);

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 850666147)
    private transient PloygonDao myDao;

    @Generated(hash = 504949523)
    public Ploygon(Long id, String name, boolean isLine, int pointCount, String comments,
            double measureResult, @NotNull String time) {
        this.id = id;
        this.name = name;
        this.isLine = isLine;
        this.pointCount = pointCount;
        this.comments = comments;
        this.measureResult = measureResult;
        this.time = time;
    }

    @Generated(hash = 1277340304)
    public Ploygon() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPointCount() {
        return this.pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1757439268)
    public List<SimplePoint> getPoints() {
        if (points == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SimplePointDao targetDao = daoSession.getSimplePointDao();
            List<SimplePoint> pointsNew = targetDao._queryPloygon_Points(id);
            synchronized (this) {
                if (points == null) {
                    points = pointsNew;
                }
            }
        }
        return points;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1076404248)
    public synchronized void resetPoints() {
        points = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 363873990)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPloygonDao() : null;
    }

    public boolean getIsLine() {
        return this.isLine;
    }

    public void setIsLine(boolean isLine) {
        this.isLine = isLine;
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public double getMeasureResult() {
        return this.measureResult;
    }

    public void setMeasureResult(double measureResult) {
        this.measureResult = measureResult;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
