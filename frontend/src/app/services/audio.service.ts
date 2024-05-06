import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AudioService {

  private url: string = `http://localhost:8080/transcripts`;

  constructor(private http: HttpClient) { }

  uploadAudio(formData: FormData){
    const headers = new HttpHeaders();
    headers.append('Content-Type', 'multipart/form-data'); 

    return this.http.post<any>(`${this.url}/upload`, formData, { headers: headers });
  }
}
