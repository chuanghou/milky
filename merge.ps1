
$CurrentCodePage = chcp
$OriginalCodePage = ($CurrentCodePage -split ' ')[($CurrentCodePage -split ' ').Length - 1]
#chcp UTF-8
chcp 65001
git add -A
git commit -m $args[0]
git push
git checkout main
git merge target
git push
git checkout target
chcp $OriginalCodePage
