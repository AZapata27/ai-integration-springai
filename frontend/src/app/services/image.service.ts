import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ImageService {

  private url: string = `http://localhost:8080/images`;

  constructor(private http: HttpClient) { }

  getImage(text: string){
    return this.http.get<any>(`${this.url}/generate?param=${text}`);
  }

}
