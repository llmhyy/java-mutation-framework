package jmutation.model.microbat;

import microbat.model.ClassLocation;

import java.util.List;
import java.util.Set;

public class PrecheckResult {
    private boolean isOverLong;
    private int totalSteps;
    private String programMessage;
    private int threadNumber;
    private Set<ClassLocation> visitedClassLocations;
    private List<String> exceedingLimitMethods;
    private List<String> loadedClasses;

    public boolean isOverLong() {
        return isOverLong;
    }

    public void setOverLong(boolean overLong) {
        isOverLong = overLong;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public String getProgramMessage() {
        return programMessage;
    }

    public void setProgramMessage(String programMessage) {
        this.programMessage = programMessage;
    }

    public int getThreadNumber() {
        return threadNumber;
    }

    public void setThreadNumber(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    public Set<ClassLocation> getVisitedClassLocations() {
        return visitedClassLocations;
    }

    public void setVisitedClassLocations(Set<ClassLocation> visitedClassLocations) {
        this.visitedClassLocations = visitedClassLocations;
    }

    public List<String> getExceedingLimitMethods() {
        return exceedingLimitMethods;
    }

    public void setExceedingLimitMethods(List<String> exceedingLimitMethods) {
        this.exceedingLimitMethods = exceedingLimitMethods;
    }

    public List<String> getLoadedClasses() {
        return loadedClasses;
    }

    public void setLoadedClasses(List<String> loadedClasses) {
        this.loadedClasses = loadedClasses;
    }

}
