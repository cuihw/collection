package com.data.collection.data.greendao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.data.collection.data.greendao.CheckPoint;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.data.greendao.MessageData;
import com.data.collection.data.greendao.PictureMap;
import com.data.collection.data.greendao.Sample;
import com.data.collection.data.greendao.TraceLocation;

import com.data.collection.data.greendao.CheckPointDao;
import com.data.collection.data.greendao.GatherPointDao;
import com.data.collection.data.greendao.MessageDataDao;
import com.data.collection.data.greendao.PictureMapDao;
import com.data.collection.data.greendao.SampleDao;
import com.data.collection.data.greendao.TraceLocationDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig checkPointDaoConfig;
    private final DaoConfig gatherPointDaoConfig;
    private final DaoConfig messageDataDaoConfig;
    private final DaoConfig pictureMapDaoConfig;
    private final DaoConfig sampleDaoConfig;
    private final DaoConfig traceLocationDaoConfig;

    private final CheckPointDao checkPointDao;
    private final GatherPointDao gatherPointDao;
    private final MessageDataDao messageDataDao;
    private final PictureMapDao pictureMapDao;
    private final SampleDao sampleDao;
    private final TraceLocationDao traceLocationDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        checkPointDaoConfig = daoConfigMap.get(CheckPointDao.class).clone();
        checkPointDaoConfig.initIdentityScope(type);

        gatherPointDaoConfig = daoConfigMap.get(GatherPointDao.class).clone();
        gatherPointDaoConfig.initIdentityScope(type);

        messageDataDaoConfig = daoConfigMap.get(MessageDataDao.class).clone();
        messageDataDaoConfig.initIdentityScope(type);

        pictureMapDaoConfig = daoConfigMap.get(PictureMapDao.class).clone();
        pictureMapDaoConfig.initIdentityScope(type);

        sampleDaoConfig = daoConfigMap.get(SampleDao.class).clone();
        sampleDaoConfig.initIdentityScope(type);

        traceLocationDaoConfig = daoConfigMap.get(TraceLocationDao.class).clone();
        traceLocationDaoConfig.initIdentityScope(type);

        checkPointDao = new CheckPointDao(checkPointDaoConfig, this);
        gatherPointDao = new GatherPointDao(gatherPointDaoConfig, this);
        messageDataDao = new MessageDataDao(messageDataDaoConfig, this);
        pictureMapDao = new PictureMapDao(pictureMapDaoConfig, this);
        sampleDao = new SampleDao(sampleDaoConfig, this);
        traceLocationDao = new TraceLocationDao(traceLocationDaoConfig, this);

        registerDao(CheckPoint.class, checkPointDao);
        registerDao(GatherPoint.class, gatherPointDao);
        registerDao(MessageData.class, messageDataDao);
        registerDao(PictureMap.class, pictureMapDao);
        registerDao(Sample.class, sampleDao);
        registerDao(TraceLocation.class, traceLocationDao);
    }
    
    public void clear() {
        checkPointDaoConfig.clearIdentityScope();
        gatherPointDaoConfig.clearIdentityScope();
        messageDataDaoConfig.clearIdentityScope();
        pictureMapDaoConfig.clearIdentityScope();
        sampleDaoConfig.clearIdentityScope();
        traceLocationDaoConfig.clearIdentityScope();
    }

    public CheckPointDao getCheckPointDao() {
        return checkPointDao;
    }

    public GatherPointDao getGatherPointDao() {
        return gatherPointDao;
    }

    public MessageDataDao getMessageDataDao() {
        return messageDataDao;
    }

    public PictureMapDao getPictureMapDao() {
        return pictureMapDao;
    }

    public SampleDao getSampleDao() {
        return sampleDao;
    }

    public TraceLocationDao getTraceLocationDao() {
        return traceLocationDao;
    }

}
