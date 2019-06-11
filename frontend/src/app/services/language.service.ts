import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Language, Localizable, Localizer } from 'yti-common-ui/types/localization';
import { BehaviorSubject, Observable } from 'rxjs';
import { getFromLocalStorage, setToLocalStorage } from 'yti-common-ui/utils/storage';

export { Language };

@Injectable()
export class LanguageService implements Localizer {

  private static readonly LANGUAGE_KEY: string = 'yti-groupmanagement.language-service.language';
  language$ = new BehaviorSubject<Language>(getFromLocalStorage(LanguageService.LANGUAGE_KEY, 'fi'));

  constructor(private translateService: TranslateService) {
    translateService.addLangs(['fi', 'en']);
    translateService.setDefaultLang('en');

    this.language$.subscribe(lang => this.translateService.use(lang));
  }

  get translateLanguage$(): Observable<Language> {
    return this.language$;
  }

  get language(): Language {
    return this.language$.getValue();
  }

  set language(language: Language) {
    if (this.language !== language) {
      this.language$.next(language);
      setToLocalStorage(LanguageService.LANGUAGE_KEY, language);
    }
  }

  translate(localizable: Localizable) {

    if (!localizable) {
      return '';
    }

    const primaryLocalization = localizable[this.language];

    if (primaryLocalization) {
      return primaryLocalization;
    } else {

      for (const [language, value] of Object.entries(localizable)) {
        if (value) {
          return `${value} (${language})`;
        }
      }

      return '';
    }
  }

  translateToGivenLanguage(localizable: Localizable, languageToUse: string | null): string {
    if (!localizable) {
      return '';
    }

    if (languageToUse) {
      const primaryLocalization = localizable[languageToUse];
      if (primaryLocalization) {
        return primaryLocalization;
      }
    }

    return this.translate(localizable);
  }
}
