import { inject } from '@angular/core';
import {
  HttpInterceptorFn
} from '@angular/common/http';
import { finalize } from 'rxjs';
import { LoadingService } from '../services/loading.service';

export const LoadingInterceptor: HttpInterceptorFn = (req, next) => {

  let loadingService: LoadingService = inject(LoadingService);
  let totalRequests: number = 0;  

  totalRequests++;
  loadingService.setLoading(true);

  return next(req).pipe(    
    finalize(() => {
      totalRequests--;            
      if (totalRequests === 0) {               
        loadingService.setLoading(false);
      }
    })
  );
}

