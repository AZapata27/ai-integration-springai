import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class GptService {

  private url: string = `http://localhost:8080/chats`;

  constructor(private http: HttpClient) { }

  getText(text: string){
    return this.http.get<any>(`${this.url}/generate?message=${text}`);
  }

  chat(message: string){
    return this.http.get<any>(`${this.url}/generateConversation?message=${message}`);
  }
}
