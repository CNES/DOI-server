import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { ConfigService } from './config.service';
import { Observable, Subject } from 'rxjs';
import { Project } from '../models/projects.model';

@Injectable({ providedIn: 'root' })
export class AdminService {

    // Subject for retrieve if user is admin
    private subjectUserIsAdmin = new Subject<any>();

    // Subject for retrieve project dois
    private subjectProjectDois = new Subject<any>();

    constructor(private http: HttpClient,
                private configService: ConfigService) {}

    /** PROJECTS
     *
     * Get all projects from server
     */
    public getProjectsFromServer() {
        return this.http.get<Project[]>(this.configService.apiBaseUrl + '/admin/projects');
    }

    /**
     * Create one project
     * 
     * @param newProjectName : project name to create
     */
    public addProject(newProjectName: string) {
        // Build body
        let body = new HttpParams()
            .set('projectName', newProjectName);

        // Build header
        let option = { headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded') };

        return this.http.post<any>(this.configService.apiBaseUrl + '/admin/projects', body.toString(), option);
    }


    /**
     * Rename one project
     * 
     * @param oldProjectSuffix : the suffix on the project to rename
     * @param newProjectName : the new project name
     */
    public renameProject(oldProjectSuffix: number, newProjectName: string) {
        // Build body
        let body = new HttpParams().set('newProjectName', newProjectName);

        // Build header
        let option = { headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded') };

        // launch the request
        return this.http.post<any>(this.configService.apiBaseUrl + '/admin/projects/' + oldProjectSuffix, body, option);
    }


    /**
     * Delete one project
     * 
     * @param projectSuffixToDelete : project suffix to delete
     */
    public deleteProject(projectSuffixToDelete: number) {
        return this.http.delete<any>(this.configService.apiBaseUrl + '/admin/projects/' + projectSuffixToDelete);
    }


    /**
    * Get project users from server
    * 
    * @param projectSuffix : Get users from this project suffix
    */
    public getProjectUsersFromServer(projectSuffix : number) {
       return this.http.get<string[]>(this.configService.apiBaseUrl + '/admin/projects/' + projectSuffix + "/users");
   }


    /** ADMINS
     *
     * Get admin users
     */
    public getAdminUsersFromServer() {
        return this.http.get<any[]>(this.configService.apiBaseUrl + '/admin/superusers');
    }

    /**
     * Check if the current connected user is administrator
     */
    public isAdminFromServer() {
        return this.http.get<any[]>(this.configService.apiBaseUrl + '/admin/roles/admin');
    }

    /**
     * Add user to administrator role
     */
     public addAdminUser(name: string) {
    // Build body
    let body = new HttpParams().set('superUserName', name);

    // Build header
    let option = { headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded') };

    // Launch the request
    return this.http.post<String[]>(this.configService.apiBaseUrl + '/admin/superusers', body, option);
    }

    /**
     * Remove user on administrator role
     */
     public removeAdminUser(name: string) {
        return this.http.delete<any>(this.configService.apiBaseUrl + '/admin/superusers/' + name);
     }


    /** USERS
     *
     * Get all users
     */
    public getUsersFromServer() {
        return this.http.get<string[]>(this.configService.apiBaseUrl + '/admin/users');
    }


    /**
     * Get user associated projects from server
     */
    public getUserProjectsFromServer(user: string) {
        return this.http.get<Project[]>(this.configService.apiBaseUrl + '/admin/projects?user=' + user);
    }


    /**
     * Add one user on one project
     * @param projectSuffix : the project suffix to add user
     * @param user : the user to add
     */
    public addprojectUser(projectSuffix: number, user: string) {
        // Build body
        let body = new HttpParams().set('user', user);
    
        // Build header
        let option = { headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded') };
    
        // Launch the request
        return this.http.post<String[]>(this.configService.apiBaseUrl + '/admin/projects/' + projectSuffix + '/users', body, option);
    }

    /**
     * Remove one user on one project
     * @param projectSuffix : the project suffix to remove user
     * @param user : the user to remove
     */
     public removeProjectUser(projectSuffix: number, user: string) {
        return this.http.delete<any>(this.configService.apiBaseUrl + '/admin/projects/' + projectSuffix + '/users/' + user);
     }


    /**
     * Get dois from project from server
     */
     public getProjectDoisFromServer(projectSuffix: number) {
        this.http.get<any[]>(this.configService.apiBaseUrl + '/admin/projects/' + projectSuffix + '/dois').forEach(elements => {
            this.subjectProjectDois.next(elements);
        })
    }

    /**
     * Get doi from project (observable / subscription)
     */
     public getProjectDois(): Observable<any[]> {
        return this.subjectProjectDois.asObservable();
    }
}