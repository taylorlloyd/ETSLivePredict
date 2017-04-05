
Paper.pdf: Paper.tex boilerplate/ sections/ figures/ data/
	latexmk -pdf -halt-on-error Paper.tex
	latexmk -c Paper.tex
	rm Paper.bbl

hook:
	ln -s ../../git-hooks/pre-commit .git/hooks/pre-commit

check:
	@echo "The following items may contain weak word usage.--------------------"
	@./checks/weasels.sh sections/*.tex 1>&2
	@echo "The following items may contain passive language.-------------------"
	@./checks/passive_voice.sh sections/*.tex 1>&2
	@echo "The following items may contain unnecessary duplication.------------"
	@perl ./checks/dups sections/*.tex 2>&2
	@echo "Checking spelling.---------------------------------------------------"
	@ispell -p terms.txt -t sections/*.tex
	@echo "Checking diction.---------------------------------------------------"
	@./checks/diction.sh sections/*.tex 1>&2

style:
	@echo "Checking for nominalizations.---------------------------------------"
	@sh style.sh sections/*.tex 1>&2

clean:
	rm -f *.out *.pdf *.aux *.dvi *.log *.blg *.bbl *.tex-e
