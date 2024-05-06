import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {

  private loading: boolean = false;

  constructor() { }

  setLoading(loading: boolean) {
    //Se agregó el timeout para evitar el error ExpressionChangedAfterItHasBeenCheckedError: Expression has changed after it was checked.
    setTimeout(()=> {
      this.loading = loading;
    }, 0);
  }

  getLoading(): boolean {
    return this.loading;
  }

}
