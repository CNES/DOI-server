import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Project } from '../models/projects.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {

    // Subject for retrieve the selected project
    private subjectSelectedProject = new Subject<Project>();
    private selectedProject!: Project;

    constructor(){
        this.selectedProject = {suffix:0, projectname: ''};
    }

    public setSelectedProject(proj: Project) {
        this.subjectSelectedProject.next(proj);
        this.selectedProject = proj;
    }

    public getSelectedProject(): Project {
        return this.selectedProject;
    }

    public getSelectedProjectObs(): Observable<Project> {
        return this.subjectSelectedProject.asObservable();
    }
}